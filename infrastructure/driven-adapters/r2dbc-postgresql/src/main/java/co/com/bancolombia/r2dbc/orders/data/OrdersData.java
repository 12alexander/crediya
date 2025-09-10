package co.com.bancolombia.r2dbc.orders.data;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("orders")
public class OrdersData {
    @Id
    private String id;

    @Column("amount")
    private BigDecimal amount;

    @Column("deadline")
    private Integer deadline;

    @Column("email_address")
    private String emailAddress;

    @Column("creation_date")
    private LocalDateTime creationDate;

    @Column("update_date")
    private LocalDateTime updateDate;

    @Column("id_status")
    private String idStatus;

    @Column("id_loan_type")
    private String idLoanType;

}
