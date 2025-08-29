package co.com.bancolombia.r2dbc.orders;

import co.com.bancolombia.r2dbc.orders.data.OrdersData;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface OrdersR2dbcRepository extends ReactiveCrudRepository<OrdersData, String>, ReactiveQueryByExampleExecutor<OrdersData> {
/*
    @Modifying
    @Query("INSERT INTO orders (id, amount, deadline, email_address, id_estado, id_tipo_prestamo, creation_date, update_date ) " +
            "VALUES (:id, :amount, :deadline, :email_address, :id_estado, :id_tipo_prestamo, :creation_date, :creation_date, :update_date)")
    Mono<Integer> createOrders(String id, BigDecimal amount, Integer deadline, String email_address, String id_estado, String id_tipo_prestamo, LocalDateTime creation_date, LocalDateTime update_date);
*/
}
