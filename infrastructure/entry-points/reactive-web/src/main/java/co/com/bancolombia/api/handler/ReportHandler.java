package co.com.bancolombia.api.handler;

import co.com.bancolombia.api.dto.response.AuthResponseDTO;
import co.com.bancolombia.api.dto.response.PendingRequestResponseDTO;
import co.com.bancolombia.api.util.ReportBuilder;
import co.com.bancolombia.api.dto.response.UserReportResponseDTO;
import co.com.bancolombia.api.enums.RolEnum;
import co.com.bancolombia.api.services.AuthServiceClient;
import co.com.bancolombia.model.orders.PendingRequest;
import co.com.bancolombia.model.orders.exceptions.UnauthorizedException;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Handler for Report operations following Single Responsibility Principle.
 * Focuses only on pending requests and reporting functionality.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportHandler {

    private final IOrdersUseCase ordersUseCase;
    private final AuthServiceClient authServiceClient;
    private final ReportBuilder reportBuilder;

    public Mono<ServerResponse> getPendingRequests(ServerRequest request) {
        String traceId = generateTraceId();
        log.info("[{}] Iniciando consulta de solicitudes pendientes", traceId);
        
        return validateUserToken(request, RolEnum.ASSESSOR.getId())
                .flatMap(authUser -> this.processPendingRequestsQuery(request, authUser, traceId))
                .flatMap(this::buildPendingRequestsResponse)
                .doOnSuccess(response -> log.info("[{}] Consulta de solicitudes pendientes completada exitosamente", traceId))
                .doOnError(error -> log.error("[{}] Error consultando solicitudes pendientes: {}", traceId, error.getMessage()));
    }

    private Mono<java.util.List<PendingRequestResponseDTO>> processPendingRequestsQuery(
            ServerRequest request, AuthResponseDTO authUser, String traceId) {
        
        String statusParam = request.queryParam("status").orElse(null);
        String emailParam = request.queryParam("email").orElse(null);
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        
        log.info("[{}] Parámetros de consulta - status: {}, email: {}, page: {}, size: {}", 
                 traceId, statusParam, emailParam, page, size);
        
        UUID statusId = statusParam != null ? UUID.fromString(statusParam) : null;
        
        return ordersUseCase.findPendingRequests(statusId, emailParam, page, size)
                .flatMap(pendingRequest -> this.enrichRequestWithUserData(authUser.getToken(), pendingRequest, traceId))
                .collectList()
                .doOnNext(requests -> log.info("[{}] Se encontraron {} solicitudes pendientes", traceId, requests.size()));
    }

    private Mono<PendingRequestResponseDTO> enrichRequestWithUserData(
            String token, PendingRequest pendingRequest, String traceId) {
        
        return authServiceClient.getUserByEmailAddress(token, pendingRequest.getEmailAddress())
                .onErrorResume(ex -> {
                    log.warn("[{}] No se pudo obtener datos del usuario para email: {}", traceId, pendingRequest.getEmailAddress());
                    return Mono.empty();
                })
                .map(user -> reportBuilder.buildFrom(pendingRequest, user))
                .defaultIfEmpty(this.convertToDTO(pendingRequest));
    }

    private Mono<ServerResponse> buildPendingRequestsResponse(java.util.List<PendingRequestResponseDTO> pendingRequests) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pendingRequests);
    }

    private PendingRequestResponseDTO convertToDTO(PendingRequest pendingRequest) {
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

    private String generateTraceId() {
        return "REPORT-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
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