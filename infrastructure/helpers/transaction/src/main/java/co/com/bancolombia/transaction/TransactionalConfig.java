package co.com.bancolombia.transaction;

import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

/**
 * Configuration for reactive transaction management.
 * Provides beans for transaction handling in R2DBC reactive applications.
 * 
 * Follows Configuration best practices:
 * - Clear separation of concerns
 * - Dependency injection for ConnectionFactory
 * - Bean lifecycle management
 */
@Configuration
@RequiredArgsConstructor
public class TransactionalConfig {
    
    private final ConnectionFactory connectionFactory;
    
    /**
     * Configure the reactive transaction manager for R2DBC operations.
     * Manages transaction boundaries for database operations.
     *
     * @return ReactiveTransactionManager instance
     */
    @Bean
    public ReactiveTransactionManager reactiveTransactionManager() {
        return new R2dbcTransactionManager(connectionFactory);
    }
    
    /**
     * Configure the transactional operator for reactive streams.
     * Provides a functional API for transaction management.
     *
     * @return TransactionalOperator instance
     */
    @Bean
    public TransactionalOperator transactionalOperator() {
        return TransactionalOperator.create(reactiveTransactionManager());
    }
}