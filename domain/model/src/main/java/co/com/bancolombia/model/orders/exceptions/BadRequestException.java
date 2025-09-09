package co.com.bancolombia.model.orders.exceptions;

public class BadRequestException extends OrdersBusinessException {
    public BadRequestException(String message) {
        super("VALIDATION_ERROR", message);
    }
    
    public BadRequestException(String code, String message) {
        super(code, message);
    }
}