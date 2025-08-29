package co.com.bancolombia.r2dbc.orders;

import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.model.orders.gateways.OrdersRepository;
import co.com.bancolombia.r2dbc.orders.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
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
    @Transactional
    public Mono<Orders> createOrder(Orders orders) {
        log.debug("Guardando Solicitud con ID: {}", orders.getId());

        Mono<Orders> saveOrder = repository.save(OrdersMapper.toDataForCreation(orders))
                .map(OrdersMapper::toDomain)
                .doOnSuccess(orderCreated ->
                        log.debug("Solicitud creada exitosamente con ID: {}", orderCreated.getId())
                )
                .doOnError(error ->
                                log.error("Error al guardar Solicitud: {}", error.getMessage())
                );
        return txOperator.transactional(saveOrder);
    }
}
