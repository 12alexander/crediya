package co.com.bancolombia.r2dbc.orders.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderPendingData {
    private BigDecimal amount;
    private Integer deadline;
    private String emailAddress;
    private String loanType;
    private BigDecimal interestRate;
    private String statusOrder;
    private BigDecimal totalMonthlyDebt;
}