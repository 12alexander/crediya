package co.com.bancolombia.model.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRequest {
    private BigDecimal amount;
    private Integer deadline;
    private String emailAddress;
    private String name;
    private String loanType;
    private BigDecimal interestRate;
    private String status;
    private BigDecimal baseSalary;
    private BigDecimal monthlyAmount;
}