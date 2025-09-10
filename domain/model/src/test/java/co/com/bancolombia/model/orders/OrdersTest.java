package co.com.bancolombia.model.orders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Orders entity.
 * Tests domain validation and business rules.
 */
class OrdersTest {

    @Test
    @DisplayName("Create new order - success")
    void createNewOrderSuccess() {
        // Arrange
        BigDecimal amount = new BigDecimal("50000.00");
        Integer deadline = 24;
        String emailAddress = "test@example.com";
        String loanTypeId = "550e8400-e29b-41d4-a716-446655441003";
        String statusId = "pending-status-id";

        // Act
        Orders order = Orders.createNew(amount, deadline, emailAddress, loanTypeId, statusId);

        // Assert
        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(amount, order.getAmount());
        assertEquals(deadline, order.getDeadline());
        assertEquals(emailAddress, order.getEmailAddress());
        assertEquals(loanTypeId, order.getIdLoanType());
        assertEquals(statusId, order.getIdStatus());
        assertNotNull(order.getCreationDate());
        assertNotNull(order.getUpdateDate());
    }

    @Test
    @DisplayName("Validate for creation - success")
    void validateForCreationSuccess() {
        // Arrange
        Orders order = Orders.builder()
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> order.validateForCreation());
    }


    @Test
    @DisplayName("Validate for creation - null amount")
    void validateForCreationNullAmount() {
        // Arrange
        Orders order = Orders.builder()
                .amount(null)
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("monto debe ser mayor que 0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1000", "-0.01"})
    @DisplayName("Validate for creation - invalid amount (zero or negative)")
    void validateForCreationInvalidAmount(String amountValue) {
        // Arrange
        Orders order = Orders.builder()
                .amount(new BigDecimal(amountValue))
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("monto debe ser mayor que 0"));
    }

    @Test
    @DisplayName("Validate for creation - too many decimals")
    void validateForCreationTooManyDecimals() {
        // Arrange
        Orders order = Orders.builder()
                .amount(new BigDecimal("50000.123")) // 3 decimals
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("más de 2 decimales"));
    }

    @Test
    @DisplayName("Validate for creation - null deadline")
    void validateForCreationNullDeadline() {
        // Arrange
        Orders order = Orders.builder()
                .amount(new BigDecimal("50000.00"))
                .deadline(null)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("plazo debe ser mayor que 0"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -12})
    @DisplayName("Validate for creation - invalid deadline (zero or negative)")
    void validateForCreationInvalidDeadline(int deadlineValue) {
        // Arrange
        Orders order = Orders.builder()
                .amount(new BigDecimal("50000.00"))
                .deadline(deadlineValue)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("plazo debe ser mayor que 0"));
    }

    @Test
    @DisplayName("Validate for creation - deadline too high")
    void validateForCreationDeadlineTooHigh() {
        // Arrange
        Orders order = Orders.builder()
                .amount(new BigDecimal("50000.00"))
                .deadline(361) // More than 360
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("no puede ser mayor a 360 meses"));
    }

    @Test
    @DisplayName("Validate for creation - null email address")
    void validateForCreationNullEmailAddress() {
        // Arrange
        Orders order = Orders.builder()
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress(null)
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("correo electrónico es obligatorio"));
    }

    @Test
    @DisplayName("Validate for creation - invalid email format")
    void validateForCreationInvalidEmailFormat() {
        // Arrange
        Orders order = Orders.builder()
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("invalid-email")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("formato del correo electrónico no es válido"));
    }

    @Test
    @DisplayName("Validate for creation - null loan type ID")
    void validateForCreationNullLoanTypeId() {
        // Arrange
        Orders order = Orders.builder()
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType(null)
                .idStatus("pending-status-id")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("tipo de préstamo es obligatorio"));
    }

    @Test
    @DisplayName("Builder pattern works correctly")
    void builderPatternWorks() {
        // Arrange & Act
        LocalDateTime now = LocalDateTime.now();
        Orders order = Orders.builder()
                .id("order-123")
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .creationDate(now)
                .updateDate(now)
                .build();

        // Assert
        assertEquals("order-123", order.getId());
        assertEquals(new BigDecimal("50000.00"), order.getAmount());
        assertEquals(24, order.getDeadline());
        assertEquals("test@example.com", order.getEmailAddress());
        assertEquals("550e8400-e29b-41d4-a716-446655441003", order.getIdLoanType());
        assertEquals("pending-status-id", order.getIdStatus());
        assertEquals(now, order.getCreationDate());
        assertEquals(now, order.getUpdateDate());
    }

    @Test
    @DisplayName("ToBuilder pattern works correctly")
    void toBuilderPatternWorks() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Orders originalOrder = Orders.builder()
                .id("order-123")
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .creationDate(now)
                .updateDate(now)
                .build();

        // Act
        Orders modifiedOrder = originalOrder.toBuilder()
                .amount(new BigDecimal("75000.00"))
                .deadline(36)
                .build();

        // Assert
        assertEquals("order-123", modifiedOrder.getId());
        assertEquals(new BigDecimal("75000.00"), modifiedOrder.getAmount()); // Modified
        assertEquals(36, modifiedOrder.getDeadline()); // Modified
        assertEquals("test@example.com", modifiedOrder.getEmailAddress());
        assertEquals("550e8400-e29b-41d4-a716-446655441003", modifiedOrder.getIdLoanType());
        assertEquals("pending-status-id", modifiedOrder.getIdStatus());
        assertEquals(now, modifiedOrder.getCreationDate());
        assertEquals(now, modifiedOrder.getUpdateDate());
    }
}