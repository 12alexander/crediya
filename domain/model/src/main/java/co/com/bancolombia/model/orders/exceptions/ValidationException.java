package co.com.bancolombia.model.orders.exceptions;

import java.util.List;

/**
 * Exception for validation errors with multiple error messages.
 * Extends OrdersBusinessException for consistent error handling.
 */
public class ValidationException extends OrdersBusinessException {
    
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("VALIDATION_ERROR", String.join("; ", errors));
        this.errors = errors;
    }

    public ValidationException(String message, List<String> errors) {
        super("VALIDATION_ERROR", message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}