package co.com.bancolombia.r2dbc.loantype.mapper;

import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.r2dbc.loantype.data.LoanTypeData;

public class LoanTypeMapper {
    
    private LoanTypeMapper(){
        throw new IllegalStateException("Utility class");
    }
    
    public static LoanType toDomain(LoanTypeData loanTypeData){
        return LoanType.builder()
                .id(loanTypeData.getId())
                .name(loanTypeData.getName())
                .minimumAmount(loanTypeData.getMinimumAmount())
                .maximumAmount(loanTypeData.getMaximumAmount())
                .interestRate(loanTypeData.getInterestRate())
                .automaticValidation(loanTypeData.getAutomaticValidation())
                .build();
    }
}