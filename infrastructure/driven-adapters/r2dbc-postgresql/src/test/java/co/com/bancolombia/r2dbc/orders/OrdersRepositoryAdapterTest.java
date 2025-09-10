package co.com.bancolombia.r2dbc.orders;

import co.com.bancolombia.model.orders.Orders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class OrdersRepositoryAdapterTest {


    private Orders buildOrdersDomain() {
        return Orders.builder()
                .id("order-123")
                .amount(new BigDecimal("50000.00"))
                .deadline(24)
                .emailAddress("test@example.com")
                .idLoanType("550e8400-e29b-41d4-a716-446655441003")
                .idStatus("pending-status-id")
                .creationDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Orders domain entity creation - success")
    void ordersDomainEntityCreationSuccess() {
        Orders order = buildOrdersDomain();

        assertNotNull(order);
        assertEquals("order-123", order.getId());
        assertEquals(new BigDecimal("50000.00"), order.getAmount());
        assertEquals(24, order.getDeadline());
        assertEquals("test@example.com", order.getEmailAddress());
        assertEquals("550e8400-e29b-41d4-a716-446655441003", order.getIdLoanType());
        assertEquals("pending-status-id", order.getIdStatus());
        assertNotNull(order.getCreationDate());
        assertNotNull(order.getUpdateDate());
    }

    @Test
    @DisplayName("Orders validation - valid order")
    void ordersValidationValidOrder() {
        Orders order = buildOrdersDomain();

        assertDoesNotThrow(() -> order.validateForCreation());
    }


    @Test
    @DisplayName("Orders validation - invalid email")
    void ordersValidationInvalidEmail() {
        Orders order = buildOrdersDomain().toBuilder()
                .emailAddress("invalid-email")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> order.validateForCreation());
        assertTrue(exception.getMessage().contains("formato del correo electrónico no es válido"));
    }

    @Test
    @DisplayName("Orders create new - success")
    void ordersCreateNewSuccess() {
        String documentId = "87654321";
        BigDecimal amount = new BigDecimal("75000.00");
        Integer deadline = 36;
        String emailAddress = "new@example.com";
        String loanTypeId = "another-loan-type-id";
        String statusId = "pending-status";

        Orders newOrder = Orders.createNew(amount, deadline, emailAddress, loanTypeId, statusId);

        assertNotNull(newOrder);
        assertNotNull(newOrder.getId());
        assertEquals(amount, newOrder.getAmount());
        assertEquals(deadline, newOrder.getDeadline());
        assertEquals(emailAddress, newOrder.getEmailAddress());
        assertEquals(loanTypeId, newOrder.getIdLoanType());
        assertEquals(statusId, newOrder.getIdStatus());
        assertNotNull(newOrder.getCreationDate());
        assertNotNull(newOrder.getUpdateDate());
    }

    @Test
    @DisplayName("Orders builder pattern - success")
    void ordersBuilderPatternSuccess() {
        LocalDateTime now = LocalDateTime.now();

        Orders order = Orders.builder()
                .id("test-order-456")
                .amount(new BigDecimal("25000.50"))
                .deadline(12)
                .emailAddress("builder@test.com")
                .idLoanType("builder-loan-type")
                .idStatus("builder-status")
                .creationDate(now)
                .updateDate(now)
                .build();

        assertEquals("test-order-456", order.getId());
        assertEquals(new BigDecimal("25000.50"), order.getAmount());
        assertEquals(12, order.getDeadline());
        assertEquals("builder@test.com", order.getEmailAddress());
        assertEquals("builder-loan-type", order.getIdLoanType());
        assertEquals("builder-status", order.getIdStatus());
        assertEquals(now, order.getCreationDate());
        assertEquals(now, order.getUpdateDate());
    }
}