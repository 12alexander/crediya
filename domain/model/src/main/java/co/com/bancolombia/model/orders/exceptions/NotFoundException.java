package co.com.bancolombia.model.orders.exceptions;

public class NotFoundException extends OrdersBusinessException {
    public NotFoundException(String message) {
        super("ORDER_NOT_FOUND", message);
    }
    
    public NotFoundException(String code, String message) {
        super(code, message);
    }
}