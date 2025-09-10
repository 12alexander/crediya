package co.com.bancolombia.api.openapi;

import co.com.bancolombia.api.handler.ReportHandler;
import co.com.bancolombia.api.dto.response.PendingRequestResponseDTO;
import co.com.bancolombia.api.dto.response.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Tag(name = "Reports", description = "Loan request reporting and analytics endpoints")
public class ReportOpenApi {

    private static final String API_V1_SOLICITUDES_PENDIENTES = "/api/v1/solicitudes-pendientes";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = API_V1_SOLICITUDES_PENDIENTES,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = ReportHandler.class,
                    beanMethod = "getPendingRequests",
                    operation = @Operation(
                            operationId = "getPendingLoanRequests",
                            summary = "Get Pending Loan Requests",
                            description = "Retrieves all pending loan requests with user information for administrative reporting",
                            tags = {"Reports"},
                            security = @SecurityRequirement(name = "bearerAuth"),
                            parameters = @Parameter(
                                    name = "Authorization",
                                    description = "Bearer token in format: Bearer {jwt-token}",
                                    required = true,
                                    in = ParameterIn.HEADER,
                                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully",
                                            content = @Content(schema = @Schema(implementation = PendingRequestResponseDTO.class))),
                                    @ApiResponse(responseCode = "401", description = "Authentication failed - invalid or missing token",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                                    @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges for reports",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                                    @ApiResponse(responseCode = "500", description = "Internal server error during report generation",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> reportRoutesDoc() {
        return RouterFunctions.route(
                RequestPredicates.GET("/__dummy_report__"),
                req -> ServerResponse.ok().build()
        );
    }
}