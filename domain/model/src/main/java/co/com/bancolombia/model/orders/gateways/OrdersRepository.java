package co.com.bancolombia.model.orders.gateways;

import co.com.bancolombia.model.orders.Orders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrdersRepository {
    Mono<Orders> save(Orders orders);
    Mono<Orders> findById(String id);
    Mono<Orders> findByDocumentId(String documentId);
    Flux<Orders> findByEmailAddress(String emailAddress);
    Mono<Boolean> existsByDocumentIdAndStatus(String documentId, String statusId);
    Mono<String> findPendingStatusId();
}
