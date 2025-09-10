package co.com.bancolombia.r2dbc.orders;

import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.model.orders.PendingRequest;
import co.com.bancolombia.model.orders.gateways.OrdersRepository;
import co.com.bancolombia.r2dbc.orders.data.OrderPendingData;
import co.com.bancolombia.r2dbc.orders.data.OrdersData;
import co.com.bancolombia.r2dbc.orders.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrdersRepositoryAdapter implements OrdersRepository {

    private final OrdersR2dbcRepository repository;
    private final TransactionalOperator txOperator;
    private final DatabaseClient databaseClient;
    private final OrdersMapper ordersMapper;

    @Override
    public Mono<Orders> save(Orders orders) {
        log.debug("Guardando solicitud con ID: {}", orders.getId());

        return txOperator.transactional(
                repository.existsById(orders.getId())
                        .defaultIfEmpty(false)
                        .doOnNext(exists -> log.debug("La solicitud con ID {} {} existe", 
                                orders.getId(), exists ? "SI" : "NO"))
                        .flatMap(exists -> {
                            OrdersData ordersData = ordersMapper.toData(orders);
                            if (exists) {
                                log.debug("Actualizando solicitud existente con ID: {}", orders.getId());
                                return repository.save(ordersData); // UPDATE
                            } else {
                                log.debug("Insertando nueva solicitud con ID: {}", orders.getId());
                                return repository.insertOrder(
                                        ordersData.getId(),
                                        ordersData.getAmount(),
                                        ordersData.getDeadline(),
                                        ordersData.getEmailAddress(),
                                        ordersData.getCreationDate(),
                                        ordersData.getUpdateDate(),
                                        ordersData.getIdStatus(),
                                        ordersData.getIdLoanType()
                                ).then(Mono.just(ordersData)); // INSERT directo
                            }
                        })
                        .map(ordersMapper::toDomain)
                        .doOnSuccess(savedOrder ->
                                log.debug("Solicitud guardada exitosamente con ID: {}", savedOrder.getId())
                        )
                        .doOnError(error ->
                                log.error("Error al guardar solicitud con ID {}: {}", orders.getId(), error.getMessage())
                        )
        );
    }

    @Override
    public Mono<Orders> findById(String id) {
        log.debug("Buscando solicitud con ID: {}", id);
        return repository.findById(id)
                .map(ordersMapper::toDomain)
                .doOnNext(order -> log.debug("Solicitud encontrada: {}", order.getId()));
    }

    @Override
    public Flux<Orders> findByEmailAddress(String emailAddress) {
        log.debug("Buscando solicitudes para email: {}", emailAddress);
        return repository.findByEmailAddress(emailAddress)
                .map(ordersMapper::toDomain)
                .doOnNext(order -> log.debug("Solicitud encontrada para email {}: {}", emailAddress, order.getId()));
    }

    @Override
    public Mono<String> findPendingStatusId() {
        log.debug("Obteniendo ID del estado PENDING");
        return repository.findPendingStatusId()
                .doOnNext(statusId -> log.debug("ID del estado PENDING: {}", statusId));
    }

    @Override
    public Flux<PendingRequest> findPendingRequests(UUID statusId, String email, int page, int size) {
        log.debug("Buscando solicitudes pendientes - statusId: {}, email: {}, page: {}, size: {}", 
                  statusId, email, page, size);
        
        String statusIdStr = statusId != null ? statusId.toString() : null;
        int offset = page * size;
        
        return repository.findPendingOrdersQuery(statusIdStr, email, offset, size)
                .map(this::mapToPendingRequest)
                .doOnNext(dto -> log.debug("Solicitud pendiente encontrada para email: {}", dto.getEmailAddress()))
                .doOnComplete(() -> log.debug("Consulta de solicitudes pendientes completada"));
    }

    private PendingRequest mapToPendingRequest(OrderPendingData data) {
        return PendingRequest.builder()
                .amount(data.getAmount())
                .deadline(data.getDeadline())
                .emailAddress(data.getEmailAddress())
                .name("")
                .loanType(data.getLoanType())
                .interestRate(data.getInterestRate())
                .status(data.getStatusOrder())
                .baseSalary(BigDecimal.ZERO)
                .monthlyAmount(data.getTotalMonthlyDebt())
                .build();
    }
}
