package co.com.bancolombia.model.orders.exceptions;

public class LoanTypeNotFoundException extends OrdersBusinessException {
    
    public LoanTypeNotFoundException(String loanTypeId) {
        super("LOAN_TYPE_NOT_FOUND", 
              String.format("El tipo de préstamo con ID '%s' no existe", loanTypeId));
    }
}