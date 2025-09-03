package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.CreateLoanRequestDTO;
import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for RouterRest.
 * Uses WebTestClient and Mockito for testing loan request endpoints.
 */
@ExtendWith(MockitoExtension.class)
class RouterRestTest {

    private WebTestClient webTestClient;
    private IOrdersUseCase ordersUseCase;
    private Validator validator;

    /**
     * Build a CreateLoanRequestDTO for testing.
     */
    private CreateLoanRequestDTO buildLoanRequest() {
        return CreateLoanRequestDTO.builder()
                .documentId("12345678")
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("test@example.com")
                .loanTypeId("550e8400-e29b-41d4-a716-446655441003")
                .build();
    }

    /**
     * Build an Orders entity from CreateLoanRequestDTO.
     */
    private Orders buildOrdersFromRequest(CreateLoanRequestDTO request) {
        return Orders.builder()
                .id("order-123")
                .documentId(request.getDocumentId())
                .amount(request.getAmount())
                .deadline(request.getDeadline())
                .emailAddress(request.getEmailAddress())
                .idLoanType(request.getLoanTypeId())
                .idStatus("pending-status-id")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    /**
     * Setup mocks and WebTestClient.
     */
    @BeforeEach
    void setup() {
        ordersUseCase = mock(IOrdersUseCase.class);
        validator = mock(Validator.class);

        Handler handler = new Handler(ordersUseCase, validator);
        RouterRest routerRest = new RouterRest();
        RouterFunction<ServerResponse> router = routerRest.routerFunction(handler);

        this.webTestClient = WebTestClient.bindToRouterFunction(router).build();
    }

    /**
     * Test successful loan request creation.
     */
    @Test
    @DisplayName("POST /api/v1/solicitud - success")
    void createLoanRequestSuccess() {
        CreateLoanRequestDTO request = buildLoanRequest();
        Orders savedOrder = buildOrdersFromRequest(request);

        // Mock validation success
        when(validator.validate(any(CreateLoanRequestDTO.class))).thenReturn(Set.of());

        // Mock use case
        when(ordersUseCase.createLoanRequest(
                anyString(), any(BigDecimal.class), any(Integer.class), anyString(), anyString()
        )).thenReturn(Mono.just(savedOrder));

        webTestClient.post()
                .uri("/api/v1/solicitud")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedOrder.getId())
                .jsonPath("$.documentId").isEqualTo(savedOrder.getDocumentId())
                .jsonPath("$.amount").isEqualTo(savedOrder.getAmount())
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    /**
     * Test successful loan request retrieval by ID.
     */
    @Test
    @DisplayName("GET /api/v1/solicitud/{id} - success")
    void getLoanRequestSuccess() {
        String orderId = "order-123";
        Orders existingOrder = Orders.builder()
                .id(orderId)
                .documentId("12345678")
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        when(ordersUseCase.findById(orderId)).thenReturn(Mono.just(existingOrder));

        webTestClient.get()
                .uri("/api/v1/solicitud/{id}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(existingOrder.getId())
                .jsonPath("$.documentId").isEqualTo(existingOrder.getDocumentId())
                .jsonPath("$.amount").isEqualTo(existingOrder.getAmount())
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    /**
     * Test basic instantiation.
     */
    @Test
    @DisplayName("RouterRest - basic instantiation")
    void routerRestBasicTest() {
        RouterRest routerRest = new RouterRest();
        assertNotNull(routerRest);
    }
}