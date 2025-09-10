package co.com.bancolombia.api.openapi;

import co.com.bancolombia.api.handler.OrderHandler;
import co.com.bancolombia.api.dto.request.CreateLoanRequestDTO;
import co.com.bancolombia.api.dto.response.LoanRequestResponseDTO;
import co.com.bancolombia.api.dto.response.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
@Tag(name = "Loan Orders", description = "Loan request creation and management endpoints")
public class OrderOpenApi {

    private static final String API_V1_SOLICITUD = "/api/v1/solicitud";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = API_V1_SOLICITUD,
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = OrderHandler.class,
                    beanMethod = "createLoanRequest",
                    operation = @Operation(
                            operationId = "createLoanRequest",
                            summary = "Create Loan Request",
                            description = "Creates a new loan request for authenticated clients",
                            tags = {"Loan Orders"},
                            security = @SecurityRequirement(name = "bearerAuth"),
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Loan request data",
                                    content = @Content(schema = @Schema(implementation = CreateLoanRequestDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Loan request created successfully",
                                            content = @Content(schema = @Schema(implementation = LoanRequestResponseDTO.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid request data",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                                    @ApiResponse(responseCode = "401", description = "Authentication failed - invalid or missing token",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                                    @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
                            }
                    )
            ),
            @RouterOperation(
                    path = API_V1_SOLICITUD + "/{id}",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = OrderHandler.class,
                    beanMethod = "getLoanRequest",
                    operation = @Operation(
                            operationId = "getLoanRequestById",
                            summary = "Get Loan Request by ID",
                            description = "Retrieves a loan request by its unique identifier",
                            tags = {"Loan Orders"},
                            security = @SecurityRequirement(name = "bearerAuth"),
                            parameters = @Parameter(
                                    name = "id", 
                                    description = "Loan request ID", 
                                    required = true, 
                                    in = ParameterIn.PATH,
                                    example = "123e4567-e89b-12d3-a456-426614174000"
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Loan request found",
                                            content = @Content(schema = @Schema(implementation = LoanRequestResponseDTO.class))),
                                    @ApiResponse(responseCode = "404", description = "Loan request not found",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
                                    @ApiResponse(responseCode = "401", description = "Authentication failed - invalid or missing token",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> orderRoutesDoc() {
        return RouterFunctions.route(
                RequestPredicates.GET("/__dummy_order__"),
                req -> ServerResponse.ok().build()
        );
    }
}