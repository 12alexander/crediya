package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.CreateLoanRequestDTO;
import co.com.bancolombia.api.dto.LoanRequestResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    
    private static final String API_V1 = "/api/v1";
    private static final String SOLICITUD_PATH = API_V1 + "/solicitud";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitud",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "createLoanRequest",
                    operation = @Operation(
                            operationId = "createLoanRequest",
                            summary = "Crear solicitud de préstamo",
                            description = "Endpoint para crear una nueva solicitud de préstamo",
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos de la solicitud de préstamo",
                                    content = @Content(schema = @Schema(implementation = CreateLoanRequestDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Solicitud creada exitosamente",
                                            content = @Content(schema = @Schema(implementation = LoanRequestResponseDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Datos de entrada inválidos"
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud/{id}",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "getLoanRequest",
                    operation = @Operation(
                            operationId = "getLoanRequest",
                            summary = "Consultar solicitud por ID",
                            description = "Endpoint para consultar una solicitud de préstamo por su ID",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Solicitud encontrada",
                                            content = @Content(schema = @Schema(implementation = LoanRequestResponseDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Solicitud no encontrada"
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(SOLICITUD_PATH)
                        .and(accept(MediaType.APPLICATION_JSON)), 
                handler::createLoanRequest)
                .andRoute(GET(SOLICITUD_PATH + "/{id}"), 
                        handler::getLoanRequest);
    }
}