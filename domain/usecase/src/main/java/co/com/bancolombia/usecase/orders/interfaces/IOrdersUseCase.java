package co.com.bancolombia.usecase.orders.interfaces;

import co.com.bancolombia.model.orders.PendingRequest;
import co.com.bancolombia.model.orders.Orders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface IOrdersUseCase {

    Mono<Orders> createLoanRequest(String idUser, BigDecimal amount, Integer deadline, 
                                  String emailAddress, String loanTypeId);

    Mono<Orders> findById(String orderId);

    Flux<Orders> findByEmailAddress(String emailAddress);

    Flux<co.com.bancolombia.model.orders.PendingRequest> findPendingRequests(UUID statusId, String email, int page, int size);
}   