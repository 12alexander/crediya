package co.com.bancolombia.model.orders.gateways;

import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.model.orders.PendingRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrdersRepository {
    Mono<Orders> save(Orders orders);
    Mono<Orders> findById(String id);
    Flux<Orders> findByEmailAddress(String emailAddress);
    Mono<String> findPendingStatusId();
    Flux<PendingRequest> findPendingRequests(UUID statusId, String email, int page, int size);
}
