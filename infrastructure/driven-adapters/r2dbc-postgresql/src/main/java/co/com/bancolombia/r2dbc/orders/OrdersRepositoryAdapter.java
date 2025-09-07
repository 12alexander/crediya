package co.com.bancolombia.r2dbc.orders;

import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.model.orders.gateways.OrdersRepository;
import co.com.bancolombia.r2dbc.orders.data.OrdersData;
import co.com.bancolombia.r2dbc.orders.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrdersRepositoryAdapter implements OrdersRepository {

    private final OrdersR2dbcRepository repository;
    private final TransactionalOperator txOperator;

    @Override
    public Mono<Orders> save(Orders orders) {
        log.debug("Guardando solicitud con ID: {} y documento: {}", orders.getId(), orders.getDocumentId());

        return txOperator.transactional(
                repository.existsById(orders.getId())
                        .defaultIfEmpty(false)
                        .doOnNext(exists -> log.debug("La solicitud con ID {} {} existe", 
                                orders.getId(), exists ? "SI" : "NO"))
                        .flatMap(exists -> {
                            OrdersData ordersData = OrdersMapper.toData(orders);
                            if (exists) {
                                log.debug("Actualizando solicitud existente con ID: {}", orders.getId());
                                return repository.save(ordersData); // UPDATE
                            } else {
                                log.debug("Insertando nueva solicitud con ID: {}", orders.getId());
                                return repository.insertOrder(
                                        ordersData.getId(),
                                        ordersData.getDocumentId(), 
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
                        .map(OrdersMapper::toDomain)
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
                .map(OrdersMapper::toDomain)
                .doOnNext(order -> log.debug("Solicitud encontrada: {}", order.getId()));
    }

    @Override
    public Mono<Orders> findByDocumentId(String documentId) {
        log.debug("Buscando solicitud para documento: {}", documentId);
        return repository.findByDocumentId(documentId)
                .map(OrdersMapper::toDomain)
                .doOnNext(order -> log.debug("Solicitud encontrada para documento {}: {}", documentId, order.getId()));
    }

    @Override
    public Flux<Orders> findByEmailAddress(String emailAddress) {
        log.debug("Buscando solicitudes para email: {}", emailAddress);
        return repository.findByEmailAddress(emailAddress)
                .map(OrdersMapper::toDomain)
                .doOnNext(order -> log.debug("Solicitud encontrada para email {}: {}", emailAddress, order.getId()));
    }

    @Override
    public Mono<Boolean> existsByDocumentIdAndStatus(String documentId, String statusId) {
        log.debug("Verificando si existe solicitud para documento {} con estado {}", documentId, statusId);
        return repository.existsByDocumentIdAndIdStatus(documentId, statusId)
                .doOnNext(exists -> log.debug("Existe solicitud para documento {} con estado {}: {}", 
                                            documentId, statusId, exists));
    }

    @Override
    public Mono<String> findPendingStatusId() {
        log.debug("Obteniendo ID del estado PENDING");
        return repository.findPendingStatusId()
                .doOnNext(statusId -> log.debug("ID del estado PENDING: {}", statusId));
    }
}
