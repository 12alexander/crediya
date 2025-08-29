package co.com.bancolombia.model.orders.gateways;

import co.com.bancolombia.model.orders.Orders;
import reactor.core.publisher.Mono;

public interface OrdersRepository {
    Mono<Orders> createOrder(Orders orders);
}
