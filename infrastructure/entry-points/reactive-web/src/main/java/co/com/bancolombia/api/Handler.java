package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.CreateLoanRequestDTO;
import co.com.bancolombia.api.dto.LoanRequestResponseDTO;
import co.com.bancolombia.api.dto.response.AuthResponseDTO;
import co.com.bancolombia.api.dto.response.PendingRequestResponseDTO;
import co.com.bancolombia.api.dto.response.UserReportResponseDTO;
import co.com.bancolombia.api.enums.RolEnum;
import co.com.bancolombia.api.services.AuthServiceClient;
import co.com.bancolombia.model.orders.PendingRequest;
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
    private final AuthServiceClient authServiceClient;

    public Mono<ServerResponse> createLoanRequest(ServerRequest request) {
        String traceId = generateTraceId();
        log.info("[{}] Iniciando procesamiento de solicitud de préstamo", traceId);
        
        return validateUserToken(request, RolEnum.CLIENT.getId())
                .flatMap(authUser -> request.bodyToMono(CreateLoanRequestDTO.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El cuerpo de la solicitud no puede estar vacío")))
                .doOnNext(dto -> log.info("[{}] Datos recibidos para documento: {}", traceId, dto.getDocumentId()))
                .flatMap(this::validateLoanRequest)
                .flatMap(dto -> processLoanRequest(dto, traceId))
                .flatMap(this::buildSuccessResponse)
                )
                .onErrorResume(this::handleError)
                .doOnSuccess(response -> log.info("[{}] Solicitud procesada exitosamente", traceId))
                .doOnError(error -> log.error("[{}] Error procesando solicitud: {}", traceId, error.getMessage()));
    }

    public Mono<ServerResponse> getLoanRequest(ServerRequest request) {
        String orderId = request.pathVariable("id");
        String traceId = generateTraceId();
        
        log.info("[{}] Consultando solicitud con ID: {}", traceId, orderId);
        
        return validateUserToken(request, RolEnum.ADMIN.getId())
                .flatMap(authUser -> ordersUseCase.findById(orderId)
                        .map(this::mapToResponseDTO)
                        .flatMap(response -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)))
                .onErrorResume(this::handleError)
                .doOnSuccess(response -> log.info("[{}] Consulta exitosa para ID: {}", traceId, orderId))
                .doOnError(error -> log.error("[{}] Error consultando solicitud {}: {}", traceId, orderId, error.getMessage()));
    }

    public Mono<ServerResponse> getPendingRequests(ServerRequest request) {
        String traceId = generateTraceId();
        log.info("[{}] Iniciando consulta de solicitudes pendientes", traceId);
        
        return validateUserToken(request, RolEnum.ASSESSOR.getId())
                .flatMap(authUser -> {
                    String statusParam = request.queryParam("status").orElse(null);
                    String emailParam = request.queryParam("email").orElse(null);
                    int page = Integer.parseInt(request.queryParam("page").orElse("0"));
                    int size = Integer.parseInt(request.queryParam("size").orElse("10"));
                    
                    log.info("[{}] Parámetros de consulta - status: {}, email: {}, page: {}, size: {}", 
                             traceId, statusParam, emailParam, page, size);
                    
                    java.util.UUID statusId = statusParam != null ? java.util.UUID.fromString(statusParam) : null;
                    
                    return ordersUseCase.findPendingRequests(statusId, emailParam, page, size)
                            .map(this::convertToDTO)
                            .flatMap(pendingRequestDTO -> 
                                    authServiceClient.getUserByEmailAddress(authUser.getToken(), pendingRequestDTO.getEmailAddress())
                                    .onErrorResume(ex -> {
                                        log.warn("[{}] No se pudo obtener datos del usuario para email: {}", traceId, pendingRequestDTO.getEmailAddress());
                                        return Mono.empty();
                                    })
                                    .map(user -> enrichPendingRequestWithUserData(pendingRequestDTO, user))
                                    .defaultIfEmpty(pendingRequestDTO)
                            )
                            .collectList()
                            .flatMap(pendingRequests -> {
                                log.info("[{}] Se encontraron {} solicitudes pendientes", traceId, pendingRequests.size());
                                return ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(pendingRequests);
                            });
                })
                .onErrorResume(this::handleError)
                .doOnSuccess(response -> log.info("[{}] Consulta de solicitudes pendientes completada exitosamente", traceId))
                .doOnError(error -> log.error("[{}] Error consultando solicitudes pendientes: {}", traceId, error.getMessage()));
    }

    private PendingRequestResponseDTO convertToDTO(
            PendingRequest pendingRequest) {
        
        return PendingRequestResponseDTO.builder()
                .amount(pendingRequest.getAmount())
                .deadline(pendingRequest.getDeadline())
                .emailAddress(pendingRequest.getEmailAddress())
                .name(pendingRequest.getName())
                .loanType(pendingRequest.getLoanType())
                .interestRate(pendingRequest.getInterestRate())
                .status(pendingRequest.getStatus())
                .baseSalary(pendingRequest.getBaseSalary())
                .monthlyAmount(pendingRequest.getMonthlyAmount())
                .build();
    }

    private PendingRequestResponseDTO enrichPendingRequestWithUserData(
            PendingRequestResponseDTO pendingRequest,
            UserReportResponseDTO user) {
        
        return PendingRequestResponseDTO.builder()
                .amount(pendingRequest.getAmount())
                .deadline(pendingRequest.getDeadline())
                .emailAddress(pendingRequest.getEmailAddress())
                .name(user.getName() + " " + user.getLastName())
                .loanType(pendingRequest.getLoanType())
                .interestRate(pendingRequest.getInterestRate())
                .status(pendingRequest.getStatus())
                .baseSalary(user.getBaseSalary())
                .monthlyAmount(pendingRequest.getMonthlyAmount())
                .build();
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

    private Mono<AuthResponseDTO> validateUserToken(ServerRequest request, java.util.UUID requiredRoleId) {
        String authHeader = request.headers().firstHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new RuntimeException("Authorization header missing or invalid"));
        }
        
        String token = authHeader.substring(7);
        
        return authServiceClient.validateToken(token)
                .flatMap(user -> {
                    boolean allowed = user.getIdRol().equals(requiredRoleId);
                    if (!allowed) {
                        return Mono.error(new RuntimeException(" Useris not allowed"));
                    }
                    return Mono.just(user);
                });
    }

    private Mono<ServerResponse> handleError(Throwable ex) {
        if (ex instanceof ConstraintViolationException ve) {
            return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(java.util.Map.of("errors", ve.getConstraintViolations().stream()
                            .map(violation -> violation.getMessage())
                            .toList()));
        } else if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized) {
            return ServerResponse.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(java.util.Map.of("errors", "Token inválido o expirado"));
        } else if (ex instanceof org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden) {
            return ServerResponse.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(java.util.Map.of("errors", "Acceso denegado"));
        } else if (ex instanceof RuntimeException && ex.getMessage().contains("Authorization header")) {
            return ServerResponse.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(java.util.Map.of("errors", "Authorization header missing or invalid"));
        } else if (ex instanceof RuntimeException && ex.getMessage().contains("not allowed")) {
            return ServerResponse.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(java.util.Map.of("errors", "Acceso denegado"));
        } else {
            return ServerResponse.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(java.util.Map.of("errors", ex.getMessage()));
        }
    }
}