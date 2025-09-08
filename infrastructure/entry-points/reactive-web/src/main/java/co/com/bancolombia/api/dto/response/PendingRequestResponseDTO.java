package co.com.bancolombia.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PendingRequestResponseDTO {
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