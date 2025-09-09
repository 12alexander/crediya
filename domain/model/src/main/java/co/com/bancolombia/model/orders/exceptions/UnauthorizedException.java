package co.com.bancolombia.model.orders.exceptions;

public class UnauthorizedException extends OrdersBusinessException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}