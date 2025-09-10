package co.com.bancolombia.r2dbc.orders;

import co.com.bancolombia.r2dbc.orders.data.OrdersData;
import co.com.bancolombia.r2dbc.orders.data.OrderPendingData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface OrdersR2dbcRepository extends ReactiveCrudRepository<OrdersData, String>, ReactiveQueryByExampleExecutor<OrdersData> {
    
    Flux<OrdersData> findByEmailAddress(String emailAddress);
    
    @Query("SELECT s.id FROM status s WHERE s.name = 'PENDING'")
    Mono<String> findPendingStatusId();
    
    @Query("INSERT INTO orders (id, amount, deadline, email_address, creation_date, update_date, id_status, id_loan_type) " +
           "VALUES ($1, $2, $3, $4, $5, $6, $7, $8)")
    Mono<Void> insertOrder(String id, BigDecimal amount, Integer deadline, 
                          String emailAddress, LocalDateTime creationDate, LocalDateTime updateDate,
                          String idStatus, String idLoanType);

    @Query("SELECT " +
           "o.amount, " +
           "o.deadline, " +
           "o.email_address, " +
           "lt.name as loan_type, " +
           "lt.interest_rate, " +
           "s.name as status_order, " +
           "ROUND((o.amount * lt.interest_rate) / o.deadline, 2) as total_monthly_debt " +
           "FROM orders o " +
           "INNER JOIN loan_type lt ON o.id_loan_type = lt.id " +
           "INNER JOIN status s ON o.id_status = s.id " +
           "WHERE ($1 IS NULL OR s.id = $1) " +
           "AND ($2 IS NULL OR o.email_address ILIKE CONCAT('%', $2, '%')) " +
           "ORDER BY o.creation_date DESC " +
           "OFFSET $3 LIMIT $4")
    Flux<OrderPendingData> findPendingOrdersQuery(String statusId, 
                                                   String email, 
                                                   int offset, 
                                                   int limit);
}
