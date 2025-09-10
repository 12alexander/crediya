package co.com.bancolombia.api.handler;

import co.com.bancolombia.api.dto.request.CreateLoanRequestDTO;
import co.com.bancolombia.api.dto.response.LoanRequestResponseDTO;
import co.com.bancolombia.api.dto.response.AuthResponseDTO;
import co.com.bancolombia.api.enums.RolEnum;
import co.com.bancolombia.api.services.AuthServiceClient;
import co.com.bancolombia.model.orders.exceptions.UnauthorizedException;
import co.com.bancolombia.transaction.TransactionalAdapter;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
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
import java.util.UUID;

/**
 * Handler for Order operations following Single Responsibility Principle.
 * Focuses only on loan request creation and retrieval.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderHandler {

    private final IOrdersUseCase ordersUseCase;
    private final Validator validator;
    private final AuthServiceClient authServiceClient;
    private final TransactionalAdapter transactionalAdapter;

    public Mono<ServerResponse> createLoanRequest(ServerRequest request) {
        String traceId = generateTraceId();
        log.info("[{}] Iniciando procesamiento de solicitud de préstamo", traceId);
        
        return validateUserToken(request, RolEnum.CLIENT.getId())
                .flatMap(authUser -> this.processLoanCreation(request, authUser, traceId))
                .flatMap(this::buildSuccessResponse)
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
                .doOnSuccess(response -> log.info("[{}] Consulta exitosa para ID: {}", traceId, orderId))
                .doOnError(error -> log.error("[{}] Error consultando solicitud {}: {}", traceId, orderId, error.getMessage()));
    }

    private Mono<LoanRequestResponseDTO> processLoanCreation(ServerRequest request, AuthResponseDTO authUser, String traceId) {
        return request.bodyToMono(CreateLoanRequestDTO.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El cuerpo de la solicitud no puede estar vacío")))
                .doOnNext(dto -> log.info("[{}] Datos recibidos para usuario: {} con email: {}", traceId, authUser.getIdUser(), dto.getEmailAddress()))
                .flatMap(this::validateLoanRequest)
                //.flatMap(dto -> this.validateClientOwnership(authUser, dto))
                .flatMap(dto -> processLoanRequest(dto, authUser.getIdUser(), traceId));
    }

    private Mono<CreateLoanRequestDTO> validateLoanRequest(CreateLoanRequestDTO dto) {
        Set<ConstraintViolation<CreateLoanRequestDTO>> violations = validator.validate(dto);
        
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Errores de validación en la solicitud");
            
            log.warn("Validation failed for loan request: {}", errorMessage);
            throw new ConstraintViolationException(violations);
        }
        
        log.debug("Validation successful for loan request");
        return Mono.just(dto);
    }

    private Mono<CreateLoanRequestDTO> validateClientOwnership(AuthResponseDTO authUser, CreateLoanRequestDTO dto) {
        String requestEmail = dto.getEmailAddress();
        log.info("Validando propiedad del cliente para email: {}", requestEmail);
        log.debug("Token del usuario autenticado: {}", authUser.getToken().substring(0, 20) + "...");
        
        return authServiceClient.getUserByEmailAddress(authUser.getToken(), requestEmail)
                .doOnNext(requestedUser -> log.info("Usuario encontrado por email: {}, ID: {}", 
                    requestedUser.getEmailAddress(), requestedUser.getId()))
                .flatMap(requestedUser -> {
                    if (!requestEmail.equals(requestedUser.getEmailAddress())) {
                        log.warn("Email solicitado {} no coincide con email del usuario {}", 
                            requestEmail, requestedUser.getEmailAddress());
                        return Mono.error(new UnauthorizedException("Los clientes solo pueden crear solicitudes de préstamo para sí mismos"));
                    }
                    log.info("Validación de propiedad exitosa para email: {}", requestEmail);
                    return Mono.just(dto);
                })
                .onErrorMap(ex -> {
                    log.error("Error en validación de propiedad del cliente: {}", ex.getMessage(), ex);
                    if (ex instanceof UnauthorizedException) {
                        return ex;
                    }
                    return new UnauthorizedException("Los clientes solo pueden crear solicitudes de préstamo para sí mismos");
                });
    }

    /**
     * Process loan request with transactional boundary.
     * Follows Single Responsibility Principle: Only handles loan processing logic
     * Uses TransactionalAdapter for proper transaction management
     */
    private Mono<LoanRequestResponseDTO> processLoanRequest(CreateLoanRequestDTO dto, UUID idUser, String traceId) {
        return transactionalAdapter.executeInTransaction(
                ordersUseCase.createLoanRequest(
                        idUser.toString(),
                        dto.getAmount(),
                        dto.getDeadline(),
                        dto.getEmailAddress(),
                        dto.getLoanTypeId()
                )
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
        return "ORDER-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    private Mono<AuthResponseDTO> validateUserToken(ServerRequest request, java.util.UUID requiredRoleId) {
        String authHeader = request.headers().firstHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new UnauthorizedException("Authorization header missing or invalid"));
        }
        
        String token = authHeader.substring(7);
        
        return authServiceClient.validateToken(token)
                .flatMap(user -> {
                    if (!user.getIdRol().equals(requiredRoleId)) {
                        return Mono.error(new UnauthorizedException("No tiene permisos para realizar esta acción"));
                    }
                    return Mono.just(user);
                });
    }
}