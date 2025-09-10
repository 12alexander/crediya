package co.com.bancolombia.api.config;

import co.com.bancolombia.api.dto.response.ErrorResponseDTO;
import co.com.bancolombia.model.orders.exceptions.OrdersBusinessException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Global exception handler for the Crediya application.
 * Provides centralized error handling for all reactive web endpoints.
 * 
 * @author Crediya Development Team
 */
@Slf4j
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    /**
     * Handles all exceptions thrown in the reactive web layer.
     * 
     * @param exchange the current server web exchange
     * @param ex the exception to handle
     * @return a Mono containing the error response
     */
    @Override
    public @NonNull Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        String path = exchange.getRequest().getPath().value();
        HttpStatus status;
        ErrorResponseDTO errorResponse;
        
        log.debug("Handling exception at {} -> {}: {}", path, ex.getClass().getName(), ex.getMessage(), ex);
        
        if (ex instanceof OrdersBusinessException businessException) {
            status = mapBusinessExceptionToStatus(businessException);
            errorResponse = ErrorResponseDTO.builder()
                    .code(businessException.getCode())
                    .message(businessException.getMessage())
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
            log.warn("{} at {} -> {}", status.value(), path, businessException.getMessage());
            
        } else if (ex instanceof IllegalArgumentException iae) {
            status = HttpStatus.BAD_REQUEST;
            errorResponse = ErrorResponseDTO.builder()
                    .code("INVALID_ARGUMENT")
                    .message(iae.getMessage())
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
            log.warn("400 at {} -> {}", path, iae.getMessage());
            
        } else if (ex instanceof ConstraintViolationException cve) {
            status = HttpStatus.BAD_REQUEST;
            String validationMessage = cve.getConstraintViolations().stream()
                    .map(v -> (v.getPropertyPath() != null ? v.getPropertyPath().toString() + ": " : "") + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Errores de validación en la solicitud");
            
            errorResponse = ErrorResponseDTO.builder()
                    .code("VALIDATION_ERROR")
                    .message(validationMessage)
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
            log.warn("400 (validation) at {} -> {}", path, validationMessage);
            
        } else if (ex instanceof UnexpectedTypeException ute) {
            status = HttpStatus.BAD_REQUEST;
            errorResponse = ErrorResponseDTO.builder()
                    .code("VALIDATION_TYPE_ERROR")
                    .message("Tipo de validación inválida: " + ute.getMessage())
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
            log.warn("400 (unexpected type) at {} -> {}", path, ute.getMessage());
            
        } else if (ex instanceof ServerWebInputException swe) {
            status = HttpStatus.BAD_REQUEST;
            errorResponse = ErrorResponseDTO.builder()
                    .code("INVALID_REQUEST_BODY")
                    .message("Cuerpo de solicitud inválido: " + swe.getReason())
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
            log.warn("400 (input) at {} -> {}", path, swe.getReason());
            
        } else if (ex instanceof DecodingException) {
            status = HttpStatus.BAD_REQUEST;
            errorResponse = ErrorResponseDTO.builder()
                    .code("JSON_DECODE_ERROR")
                    .message("No se pudo procesar el JSON. Verifique el formato y los tipos de datos.")
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
            log.warn("400 (decode) at {} -> JSON decode error", path);
            
        } else if (ex instanceof UnsupportedMediaTypeStatusException) {
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            errorResponse = ErrorResponseDTO.builder()
                    .code("UNSUPPORTED_MEDIA_TYPE")
                    .message("Content-Type no soportado. Use application/json.")
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
            log.warn("415 at {} -> Unsupported media type", path);
            
        } else if (ex instanceof NotAcceptableStatusException) {
            status = HttpStatus.NOT_ACCEPTABLE;
            errorResponse = ErrorResponseDTO.builder()
                    .code("NOT_ACCEPTABLE")
                    .message("Accept header no soportado por el servidor.")
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
            log.warn("406 at {} -> Not acceptable", path);
            
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse = ErrorResponseDTO.builder()
                    .code("INTERNAL_ERROR")
                    .message("Error interno del servidor. Por favor contacte al administrador.")
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
            log.error("500 at {} -> {}: {}", path, ex.getClass().getName(), ex.getMessage(), ex);
        }
        
        var response = exchange.getResponse();
        if (response.isCommitted()) {
            log.warn("Response already committed for path {}. Cannot write error response.", path);
            return Mono.error(ex);
        }
        
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String jsonResponse = buildJsonResponse(errorResponse);
        DataBufferFactory bufferFactory = response.bufferFactory();
        var buffer = bufferFactory.wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * Maps business exceptions to appropriate HTTP status codes.
     * 
     * @param exception the business exception
     * @return the corresponding HTTP status
     */
    private HttpStatus mapBusinessExceptionToStatus(OrdersBusinessException exception) {
        return switch (exception.getCode()) {
            case "LOAN_TYPE_NOT_FOUND", "ORDER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INVALID_LOAN_AMOUNT", "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "PENDING_STATUS_NOT_FOUND" -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
    
    /**
     * Builds a JSON response from the error response DTO.
     * 
     * @param errorResponse the error response DTO
     * @return the JSON string
     */
    private String buildJsonResponse(ErrorResponseDTO errorResponse) {
        return String.format("""
                {
                  "code": "%s",
                  "message": "%s",
                  "path": "%s",
                  "timestamp": "%s"
                }
                """, 
                escapeJson(errorResponse.getCode()),
                escapeJson(errorResponse.getMessage()),
                escapeJson(errorResponse.getPath()),
                errorResponse.getTimestamp().toString()
        );
    }
    
    /**
     * Escapes special characters in JSON strings.
     * 
     * @param str the string to escape
     * @return the escaped string
     */
    private String escapeJson(String str) {
        return str == null ? "" : str.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}