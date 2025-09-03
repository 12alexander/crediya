package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.CreateLoanRequestDTO;
import co.com.bancolombia.api.dto.LoanRequestResponseDTO;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Tag(name = "Solicitudes de Préstamo", description = "Operaciones relacionadas con la gestión de solicitudes de préstamo")
public class Handler {
    
    private final IOrdersUseCase ordersUseCase;
    private final Validator validator;

    public Mono<ServerResponse> createLoanRequest(ServerRequest request) {
        String traceId = generateTraceId();
        log.info("[{}] Iniciando procesamiento de solicitud de préstamo", traceId);
        
        return request.bodyToMono(CreateLoanRequestDTO.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El cuerpo de la solicitud no puede estar vacío")))
                .doOnNext(dto -> log.info("[{}] Datos recibidos para documento: {}", traceId, dto.getDocumentId()))
                .flatMap(this::validateLoanRequest)
                .flatMap(dto -> processLoanRequest(dto, traceId))
                .flatMap(this::buildSuccessResponse)
                .doOnSuccess(response -> log.info("[{}] Solicitud procesada exitosamente", traceId))
                .doOnError(error -> log.error("[{}] Error procesando solicitud: {}", traceId, error.getMessage()));
    }

    public Mono<ServerResponse> getLoanRequest(ServerRequest request) {
        String orderId = request.pathVariable("id");
        String traceId = generateTraceId();
        
        log.info("[{}] Consultando solicitud con ID: {}", traceId, orderId);
        
        return ordersUseCase.findById(orderId)
                .map(this::mapToResponseDTO)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnSuccess(response -> log.info("[{}] Consulta exitosa para ID: {}", traceId, orderId))
                .doOnError(error -> log.error("[{}] Error consultando solicitud {}: {}", traceId, orderId, error.getMessage()));
    }

    private Mono<CreateLoanRequestDTO> validateLoanRequest(CreateLoanRequestDTO dto) {
        Set<ConstraintViolation<CreateLoanRequestDTO>> violations = validator.validate(dto);
        
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Errores de validación en la solicitud");
            
            log.warn("Validation failed for document {}: {}", dto.getDocumentId(), errorMessage);
            throw new ConstraintViolationException(violations);
        }
        
        log.debug("Validation successful for document: {}", dto.getDocumentId());
        return Mono.just(dto);
    }

    private Mono<LoanRequestResponseDTO> processLoanRequest(CreateLoanRequestDTO dto, String traceId) {
        return ordersUseCase.createLoanRequest(
                        dto.getDocumentId(),
                        dto.getAmount(),
                        dto.getDeadline(),
                        dto.getEmailAddress(),
                        dto.getLoanTypeId()
                )
                .map(this::mapToResponseDTO)
                .doOnNext(response -> log.info("[{}] Solicitud creada con ID: {}", traceId, response.getId()));
    }

    private Mono<ServerResponse> buildSuccessResponse(LoanRequestResponseDTO responseDTO) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(responseDTO);
    }

    private LoanRequestResponseDTO mapToResponseDTO(co.com.bancolombia.model.orders.Orders order) {
        return LoanRequestResponseDTO.builder()
                .id(order.getId())
                .documentId(order.getDocumentId())
                .amount(order.getAmount())
                .deadline(order.getDeadline())
                .emailAddress(order.getEmailAddress())
                .status("PENDING")
                .loanType(order.getIdLoanType())
                .creationDate(order.getCreationDate())
                .updateDate(order.getUpdateDate())
                .build();
    }

    private String generateTraceId() {
        return "TRACE-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
}