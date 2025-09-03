package co.com.bancolombia.model.orders.exceptions;

public class OrdersBusinessException extends RuntimeException {
    
    private final String code;
    
    public OrdersBusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public OrdersBusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}