package co.com.bancolombia.r2dbc.orders;

import co.com.bancolombia.r2dbc.orders.data.OrdersData;
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
    
    Mono<OrdersData> findByDocumentId(String documentId);
    
    Flux<OrdersData> findByEmailAddress(String emailAddress);
    
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM orders o " +
           "WHERE o.document_id = :documentId AND o.id_status = :statusId")
    Mono<Boolean> existsByDocumentIdAndIdStatus(String documentId, String statusId);
    
    @Query("SELECT s.id FROM status s WHERE s.name = 'PENDING'")
    Mono<String> findPendingStatusId();
    
    @Query("INSERT INTO orders (id, document_id, amount, deadline, email_address, creation_date, update_date, id_status, id_loan_type) " +
           "VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)")
    Mono<Void> insertOrder(String id, String documentId, BigDecimal amount, Integer deadline, 
                          String emailAddress, LocalDateTime creationDate, LocalDateTime updateDate,
                          String idStatus, String idLoanType);
}
