package co.com.bancolombia.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TransactionalAdapter provides a clean abstraction for transaction management in reactive applications.
 * Follows SOLID principles:
 * - Single Responsibility: Handles only transaction boundaries
 * - Open/Closed: Extensible for different transaction types
 * - Dependency Inversion: Depends on TransactionalOperator abstraction
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionalAdapter {
    
    private final TransactionalOperator transactionalOperator;
    
    /**
     * Execute a Mono operation within a transaction boundary.
     * Ensures atomicity for single-item reactive operations.
     *
     * @param publisher The reactive publisher to execute
     * @param <T> The type of the emitted item
     * @return A transactional Mono
     */
    public <T> Mono<T> executeInTransaction(Mono<T> publisher) {
        return transactionalOperator.transactional(publisher)
                .doOnSuccess(result -> log.debug("Transaction completed successfully"))
                .doOnError(throwable -> log.error("Transaction failed: {}", throwable.getMessage()));
    }
    
    /**
     * Execute a Flux operation within a transaction boundary.
     * Ensures atomicity for multi-item reactive operations.
     *
     * @param publisher The reactive publisher to execute
     * @param <T> The type of the emitted items
     * @return A transactional Flux
     */
    public <T> Flux<T> executeInTransaction(Flux<T> publisher) {
        return transactionalOperator.transactional(publisher)
                .doOnComplete(() -> log.debug("Transactional flux completed successfully"))
                .doOnError(throwable -> log.error("Transactional flux failed: {}", throwable.getMessage()));
    }
}