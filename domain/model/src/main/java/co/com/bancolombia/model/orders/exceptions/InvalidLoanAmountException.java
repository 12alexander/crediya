package co.com.bancolombia.model.orders.exceptions;

import java.math.BigDecimal;

public class InvalidLoanAmountException extends OrdersBusinessException {
    
    public InvalidLoanAmountException(BigDecimal amount, BigDecimal minAmount, BigDecimal maxAmount) {
        super("INVALID_LOAN_AMOUNT", 
              String.format("El monto solicitado $%,.2f no está dentro del rango permitido ($%,.2f - $%,.2f)", 
                          amount, minAmount, maxAmount));
    }
}