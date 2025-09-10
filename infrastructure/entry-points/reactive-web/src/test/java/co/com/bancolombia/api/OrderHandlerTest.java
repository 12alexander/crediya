package co.com.bancolombia.api;


import co.com.bancolombia.api.dto.request.CreateLoanRequestDTO;
import co.com.bancolombia.api.dto.response.AuthResponseDTO;
import co.com.bancolombia.api.enums.RolEnum;
import co.com.bancolombia.api.handler.OrderHandler;
import co.com.bancolombia.api.services.AuthServiceClient;
import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.model.orders.exceptions.UnauthorizedException;
import co.com.bancolombia.transaction.TransactionalAdapter;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import jakarta.validation.ConstraintViolationException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderHandlerTest {

    @Mock
    private IOrdersUseCase ordersUseCase;

    @Mock
    private Validator validator;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private TransactionalAdapter transactionalAdapter;

    private OrderHandler handler;

    @BeforeEach
    void init() {
        handler = new OrderHandler(ordersUseCase, validator, authServiceClient, transactionalAdapter);
    }

    private Orders buildOrder() {
        return Orders.builder()
                .id("order-123")
                .amount(BigDecimal.TEN)
                .deadline(12)
                .emailAddress("client@test.com")
                .idLoanType("loan-type-1")
                .idStatus("PENDING")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    private AuthResponseDTO buildAuthUser(UUID roleId) {
        return AuthResponseDTO.builder()
                .idUser(UUID.randomUUID())
                .idRol(roleId)
                .token("valid-token")
                .build();
    }
/*
    @Test
    @DisplayName("createLoanRequest - éxito")
    void createLoanRequestSuccess() {
        // Arrange
        CreateLoanRequestDTO dto = CreateLoanRequestDTO.builder()
                .amount(BigDecimal.TEN)
                .deadline(12)
                .emailAddress("client@test.com")
                .loanTypeId("loan-type-1")
                .build();

        AuthResponseDTO authUser = AuthResponseDTO.builder()
                .idUser(UUID.randomUUID())
                .idRol(RolEnum.CLIENT.getId())
                .token("valid-token")
                .build();

        Orders order = Orders.builder()
                .id(UUID.randomUUID().toString())
                .amount(BigDecimal.TEN)
                .deadline(12)
                .emailAddress("client@test.com")
                .idLoanType("loan-type-1")
                .build();

        // Mocks
        when(authServiceClient.validateToken(anyString()))
                .thenReturn(Mono.just(authUser));

        when(validator.validate(any(CreateLoanRequestDTO.class)))
                .thenReturn(Collections.emptySet());

        when(authServiceClient.getUserByEmailAddress(anyString(), anyString()))
                .thenReturn(Mono.just(authUser));

        when(ordersUseCase.createLoanRequest(anyString(), any(), anyInt(), anyString(), anyString()))
                .thenReturn(Mono.just(order));

        when(transactionalAdapter.executeInTransaction(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MockServerRequest request = MockServerRequest.builder()
                .header("Authorization", "Bearer valid-token")
                .body(Mono.just(dto));

        // Act
        Mono<ServerResponse> response = handler.createLoanRequest(request);

        // Assert
        StepVerifier.create(response)
                .assertNext(res -> {
                    assert res.statusCode().is2xxSuccessful();
                    assert MediaType.APPLICATION_JSON.equals(res.headers().getContentType());
                })
                .verifyComplete();
    }*/

/*
    @Test
    @DisplayName("createLoanRequest - sin Authorization header -> error")
    void createLoanRequestUnauthorized() {
        MockServerRequest request = MockServerRequest.builder().build();

        StepVerifier.create(handler.createLoanRequest(request))
                .expectError(UnauthorizedException.class)
                .verify();
    }*/

    @Test
    @DisplayName("getLoanRequest - éxito")
    void getLoanRequestSuccess() {
        Orders order = buildOrder();
        AuthResponseDTO authUser = buildAuthUser(RolEnum.ADMIN.getId());

        when(authServiceClient.validateToken("valid-token")).thenReturn(Mono.just(authUser));
        when(ordersUseCase.findById("order-123")).thenReturn(Mono.just(order));

        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "order-123")
                .header("Authorization", "Bearer valid-token")
                .build();

        Mono<ServerResponse> response = handler.getLoanRequest(request);

        StepVerifier.create(response)
                .assertNext(res -> {
                    assert res.statusCode().is2xxSuccessful();
                    assert res.headers().getContentType().equals(MediaType.APPLICATION_JSON);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getLoanRequest - rol inválido -> error")
    void getLoanRequestInvalidRole() {
        AuthResponseDTO authUser = buildAuthUser(RolEnum.CLIENT.getId()); // No es ADMIN

        when(authServiceClient.validateToken("valid-token")).thenReturn(Mono.just(authUser));

        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", "order-123")
                .header("Authorization", "Bearer valid-token")
                .build();

        StepVerifier.create(handler.getLoanRequest(request))
                .expectError(UnauthorizedException.class)
                .verify();
    }
/*
    @Test
    @DisplayName("createLoanRequest - validación con errores -> ConstraintViolationException")
    void createLoanRequestValidationError() {
        CreateLoanRequestDTO dto = CreateLoanRequestDTO.builder()
                .amount(BigDecimal.ZERO)
                .deadline(0)
                .emailAddress("bad-email")
                .loanTypeId("loan-type-1")
                .build();

        AuthResponseDTO authUser = buildAuthUser(RolEnum.CLIENT.getId());

        ConstraintViolation<CreateLoanRequestDTO> violation = mock(ConstraintViolation.class);

        when(authServiceClient.validateToken("valid-token")).thenReturn(Mono.just(authUser));
        when(validator.validate(dto)).thenReturn(Set.of(violation));

        MockServerRequest request = MockServerRequest.builder()
                .header("Authorization", "Bearer valid-token")
                .body(Mono.just(dto));

        StepVerifier.create(handler.createLoanRequest(request))
                .expectError(ConstraintViolationException.class)
                .verify();
    }*/
}