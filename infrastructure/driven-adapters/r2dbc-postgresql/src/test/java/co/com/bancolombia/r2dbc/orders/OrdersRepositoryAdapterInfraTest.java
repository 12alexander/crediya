package co.com.bancolombia.r2dbc.orders;

import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.r2dbc.orders.data.OrderPendingData;
import co.com.bancolombia.r2dbc.orders.data.OrdersData;
import co.com.bancolombia.r2dbc.orders.mapper.OrdersMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersRepositoryAdapterInfraTest {

    @Mock
    private OrdersR2dbcRepository repository;

    @Mock
    private OrdersMapper ordersMapper;

    @Mock
    private TransactionalOperator txOperator;

    @Mock
    private DatabaseClient databaseClient;

    private OrdersRepositoryAdapter adapter;

    @BeforeEach
    void init() {
        adapter = new OrdersRepositoryAdapter(repository, txOperator, databaseClient, ordersMapper);

        // Evitamos transacción real, devolvemos el mismo Mono
        lenient().when(txOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Orders buildOrder(String id) {
        return Orders.builder()
                .id(id)
                .amount(BigDecimal.TEN)
                .deadline(12)
                .emailAddress("test@example.com")
                .idLoanType("loan-123")
                .idStatus("pending-status")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    private OrdersData buildOrderData(String id) {
        return OrdersData.builder()
                .id(id)
                .amount(BigDecimal.TEN)
                .deadline(12)
                .emailAddress("test@example.com")
                .idLoanType("loan-123")
                .idStatus("pending-status")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Guardar orden nueva - inserta cuando no existe")
    void saveInsertWhenNotExists() {
        Orders order = buildOrder("order-1");
        OrdersData data = buildOrderData("order-1");

        when(repository.existsById("order-1")).thenReturn(Mono.just(false));
        when(ordersMapper.toData(order)).thenReturn(data);
        when(repository.insertOrder(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.empty());
        when(ordersMapper.toDomain(data)).thenReturn(order);

        StepVerifier.create(adapter.save(order))
                .expectNext(order)
                .verifyComplete();

        verify(repository).insertOrder(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Guardar orden existente - actualiza cuando sí existe")
    void saveUpdateWhenExists() {
        Orders order = buildOrder("order-2");
        OrdersData data = buildOrderData("order-2");

        when(repository.existsById("order-2")).thenReturn(Mono.just(true));
        when(ordersMapper.toData(order)).thenReturn(data);
        when(repository.save(data)).thenReturn(Mono.just(data));
        when(ordersMapper.toDomain(data)).thenReturn(order);

        StepVerifier.create(adapter.save(order))
                .expectNext(order)
                .verifyComplete();

        verify(repository).save(data);
    }

    @Test
    @DisplayName("Buscar por ID - éxito")
    void findByIdSuccess() {
        OrdersData data = buildOrderData("order-3");
        Orders order = buildOrder("order-3");

        when(repository.findById("order-3")).thenReturn(Mono.just(data));
        when(ordersMapper.toDomain(data)).thenReturn(order);

        StepVerifier.create(adapter.findById("order-3"))
                .expectNext(order)
                .verifyComplete();

        verify(repository).findById("order-3");
    }

    @Test
    @DisplayName("Buscar por email - éxito")
    void findByEmailSuccess() {
        OrdersData data = buildOrderData("order-4");
        Orders order = buildOrder("order-4");

        when(repository.findByEmailAddress("test@example.com")).thenReturn(Flux.just(data));
        when(ordersMapper.toDomain(data)).thenReturn(order);

        StepVerifier.create(adapter.findByEmailAddress("test@example.com"))
                .expectNext(order)
                .verifyComplete();

        verify(repository).findByEmailAddress("test@example.com");
    }

    @Test
    @DisplayName("Obtener estado pendiente - éxito")
    void findPendingStatusIdSuccess() {
        String statusId = UUID.randomUUID().toString();
        when(repository.findPendingStatusId()).thenReturn(Mono.just(statusId));

        StepVerifier.create(adapter.findPendingStatusId())
                .expectNext(statusId)
                .verifyComplete();

        verify(repository).findPendingStatusId();
    }

    @Test
    @DisplayName("Buscar solicitudes pendientes - éxito")
    void findPendingRequestsSuccess() {
        UUID statusId = UUID.randomUUID();

        OrderPendingData data = new OrderPendingData();
        data.setAmount(BigDecimal.TEN);
        data.setDeadline(12);
        data.setEmailAddress("test@example.com");
        data.setLoanType("personal");
        data.setInterestRate(new BigDecimal("0.15"));
        data.setStatusOrder("PENDING");
        data.setTotalMonthlyDebt(new BigDecimal("500"));

        when(repository.findPendingOrdersQuery(any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(data));

        StepVerifier.create(adapter.findPendingRequests(statusId, "test@example.com", 0, 10))
                .expectNextMatches(req ->
                        req.getEmailAddress().equals("test@example.com") &&
                                req.getStatus().equals("PENDING")
                )
                .verifyComplete();

        verify(repository).findPendingOrdersQuery(any(), any(), anyInt(), anyInt());
    }
}
