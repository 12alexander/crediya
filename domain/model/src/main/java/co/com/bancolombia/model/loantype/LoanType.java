package co.com.bancolombia.model.loantype;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class LoanType {
    private String id;
    private String name;
    private BigDecimal minimumAmount;
    private BigDecimal maximumAmount;
    private BigDecimal interestRate;
    private Boolean automaticValidation;
    
    public boolean isAmountValid(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(minimumAmount) >= 0 && amount.compareTo(maximumAmount) <= 0;
    }
}