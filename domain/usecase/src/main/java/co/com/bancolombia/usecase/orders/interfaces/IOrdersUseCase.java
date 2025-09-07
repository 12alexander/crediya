package co.com.bancolombia.usecase.orders.interfaces;

import co.com.bancolombia.model.orders.Orders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface IOrdersUseCase {

    Mono<Orders> createLoanRequest(String documentId, BigDecimal amount, Integer deadline, 
                                  String emailAddress, String loanTypeId);

    Mono<Orders> findById(String orderId);

    Mono<Orders> findByDocumentId(String documentId);

    Flux<Orders> findByEmailAddress(String emailAddress);

    Mono<Boolean> existsByDocumentIdAndStatus(String documentId, String statusId);
}