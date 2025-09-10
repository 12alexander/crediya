package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.CreateLoanRequestDTO;
import co.com.bancolombia.api.dto.response.AuthResponseDTO;
import co.com.bancolombia.api.dto.response.UserReportResponseDTO;
import co.com.bancolombia.api.util.ReportBuilder;
import co.com.bancolombia.api.handler.OrderHandler;
import co.com.bancolombia.api.handler.ReportHandler;
import co.com.bancolombia.api.services.AuthServiceClient;
import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.transaction.TransactionalAdapter;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RouterRestTest {

    private WebTestClient webTestClient;
    private IOrdersUseCase ordersUseCase;
    private Validator validator;
    private AuthServiceClient authServiceClient;
    private ReportBuilder reportBuilder;
    private TransactionalAdapter transactionalAdapter;

    private CreateLoanRequestDTO buildLoanRequest() {
        return CreateLoanRequestDTO.builder()
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("test@example.com")
                .loanTypeId("550e8400-e29b-41d4-a716-446655441003")
                .build();
    }

    private Orders buildOrdersFromRequest(CreateLoanRequestDTO request) {
        return Orders.builder()
                .id("order-123")
                .amount(request.getAmount())
                .deadline(request.getDeadline())
                .emailAddress(request.getEmailAddress())
                .idLoanType(request.getLoanTypeId())
                .idStatus("pending-status-id")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setup() {
        ordersUseCase = mock(IOrdersUseCase.class);
        validator = mock(Validator.class);
        authServiceClient = mock(AuthServiceClient.class);
        reportBuilder = mock(ReportBuilder.class);
        transactionalAdapter = mock(TransactionalAdapter.class);

        OrderHandler orderHandler = new OrderHandler(ordersUseCase, validator, authServiceClient, transactionalAdapter);
        ReportHandler reportHandler = new ReportHandler(ordersUseCase, authServiceClient, reportBuilder);
        RouterRest routerRest = new RouterRest(orderHandler, reportHandler);
        RouterFunction<ServerResponse> router = routerRest.routerFunction();

        this.webTestClient = WebTestClient.bindToRouterFunction(router).build();
    }

    @Test
    @DisplayName("POST /api/v1/solicitud - success")
    void createLoanRequestSuccess() {
        CreateLoanRequestDTO request = buildLoanRequest();
        Orders savedOrder = buildOrdersFromRequest(request);

        AuthResponseDTO mockClientResponse = AuthResponseDTO.builder()
                .idUser(UUID.randomUUID())
                .idRol(UUID.fromString("b71ed6c9-1dd9-4c14-8a4a-fe06166d5cdb")) // CLIENT
                .nameUser("Test Client User")
                .token("mock-jwt-token")
                .build();
        UserReportResponseDTO mockUserResponse = UserReportResponseDTO.builder()
                .id(mockClientResponse.getIdUser().toString())
                .name("Test")
                .lastName("User")
                .emailAddress("test@example.com")
                .baseSalary(new BigDecimal("50000"))
                .build();

        Mockito.lenient().when(authServiceClient.validateToken(anyString()))
                .thenReturn(Mono.just(mockClientResponse));

        Mockito.lenient().when(authServiceClient.getUserByEmailAddress(anyString(), anyString()))
                .thenReturn(Mono.just(mockUserResponse));

        when(validator.validate(any(CreateLoanRequestDTO.class))).thenReturn(Set.of());

        when(ordersUseCase.createLoanRequest(
                anyString(), any(BigDecimal.class), any(Integer.class), anyString(), anyString()
        )).thenReturn(Mono.just(savedOrder));
        
        when(transactionalAdapter.executeInTransaction(any(Mono.class))).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        webTestClient.post()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer mock-jwt-token")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedOrder.getId())
                .jsonPath("$.amount").isEqualTo(savedOrder.getAmount().doubleValue())
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    @DisplayName("GET /api/v1/solicitud/{id} - success")
    void getLoanRequestSuccess() {
        String orderId = "order-123";
        Orders existingOrder = Orders.builder()
                .id(orderId)
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        AuthResponseDTO mockAdminResponse = AuthResponseDTO.builder()
                .idUser(UUID.randomUUID())
                .idRol(UUID.fromString("80e86d27-20a4-44be-b90d-44eeb378d409")) // ADMIN
                .nameUser("Test Admin User")
                .token("mock-token")
                .build();
        when(authServiceClient.validateToken(anyString()))
                .thenReturn(Mono.just(mockAdminResponse));

        when(ordersUseCase.findById(orderId)).thenReturn(Mono.just(existingOrder));

        webTestClient.get()
                .uri("/api/v1/solicitud/{id}", orderId)
                .header("Authorization", "Bearer mock-jwt-token")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(existingOrder.getId())
                .jsonPath("$.amount").isEqualTo(existingOrder.getAmount().doubleValue())
                .jsonPath("$.status").isEqualTo("PENDING");
    }


    @Test
    @DisplayName("RouterRest - basic instantiation")
    void routerRestBasicTest() {
        OrderHandler orderHandler = mock(OrderHandler.class);
        ReportHandler reportHandler = mock(ReportHandler.class);
        RouterRest routerRest = new RouterRest(orderHandler, reportHandler);
        assertNotNull(routerRest);
    }
}