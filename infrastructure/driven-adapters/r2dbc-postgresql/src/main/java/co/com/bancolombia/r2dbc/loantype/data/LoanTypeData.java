package co.com.bancolombia.r2dbc.loantype.data;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table("loan_type")
public class LoanTypeData {
    @Id
    private String id;

    @Column("name")
    private String name;

    @Column("minimum_amount")
    private BigDecimal minimumAmount;

    @Column("maximum_amount")
    private BigDecimal maximumAmount;

    @Column("interest_rate")
    private BigDecimal interestRate;

    @Column("automatic_validation")
    private Boolean automaticValidation;
}