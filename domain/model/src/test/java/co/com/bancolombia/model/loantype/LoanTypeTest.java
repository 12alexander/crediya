package co.com.bancolombia.model.loantype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoanType entity.
 * Tests domain validation and business rules.
 */
class LoanTypeTest {

    @Test
    @DisplayName("Create loan type - success")
    void createLoanTypeSuccess() {
        // Arrange & Act
        LoanType loanType = LoanType.builder()
                .id("550e8400-e29b-41d4-a716-446655441003")
                .name("MICROCREDITO")
                .minimumAmount(new BigDecimal("10000"))
                .maximumAmount(new BigDecimal("500000"))
                .interestRate(new BigDecimal("12.5"))
                .automaticValidation(true)
                .build();

        // Assert
        assertNotNull(loanType);
        assertEquals("550e8400-e29b-41d4-a716-446655441003", loanType.getId());
        assertEquals("MICROCREDITO", loanType.getName());
        assertEquals(new BigDecimal("10000"), loanType.getMinimumAmount());
        assertEquals(new BigDecimal("500000"), loanType.getMaximumAmount());
        assertEquals(new BigDecimal("12.5"), loanType.getInterestRate());
        assertTrue(loanType.getAutomaticValidation());
    }

    @Test
    @DisplayName("Amount validation - valid amount within range")
    void isAmountValidWithinRange() {
        // Arrange
        LoanType loanType = LoanType.builder()
                .id("550e8400-e29b-41d4-a716-446655441003")
                .name("MICROCREDITO")
                .minimumAmount(new BigDecimal("10000"))
                .maximumAmount(new BigDecimal("500000"))
                .build();

        // Act & Assert
        assertTrue(loanType.isAmountValid(new BigDecimal("50000")));
        assertTrue(loanType.isAmountValid(new BigDecimal("10000"))); // Minimum boundary
        assertTrue(loanType.isAmountValid(new BigDecimal("500000"))); // Maximum boundary
        assertTrue(loanType.isAmountValid(new BigDecimal("255000"))); // Middle value
    }

    @Test
    @DisplayName("Amount validation - amount below minimum")
    void isAmountValidBelowMinimum() {
        // Arrange
        LoanType loanType = LoanType.builder()
                .id("550e8400-e29b-41d4-a716-446655441003")
                .name("MICROCREDITO")
                .minimumAmount(new BigDecimal("10000"))
                .maximumAmount(new BigDecimal("500000"))
                .build();

        // Act & Assert
        assertFalse(loanType.isAmountValid(new BigDecimal("9999")));
        assertFalse(loanType.isAmountValid(new BigDecimal("5000")));
        assertFalse(loanType.isAmountValid(new BigDecimal("0")));
    }

    @Test
    @DisplayName("Amount validation - amount above maximum")
    void isAmountValidAboveMaximum() {
        // Arrange
        LoanType loanType = LoanType.builder()
                .id("550e8400-e29b-41d4-a716-446655441003")
                .name("MICROCREDITO")
                .minimumAmount(new BigDecimal("10000"))
                .maximumAmount(new BigDecimal("500000"))
                .build();

        // Act & Assert
        assertFalse(loanType.isAmountValid(new BigDecimal("500001")));
        assertFalse(loanType.isAmountValid(new BigDecimal("600000")));
        assertFalse(loanType.isAmountValid(new BigDecimal("1000000")));
    }

    @Test
    @DisplayName("Amount validation with decimal values")
    void isAmountValidWithDecimals() {
        // Arrange
        LoanType loanType = LoanType.builder()
                .id("550e8400-e29b-41d4-a716-446655441003")
                .name("MICROCREDITO")
                .minimumAmount(new BigDecimal("10000.00"))
                .maximumAmount(new BigDecimal("500000.00"))
                .build();

        // Act & Assert
        assertTrue(loanType.isAmountValid(new BigDecimal("50000.50")));
        assertTrue(loanType.isAmountValid(new BigDecimal("10000.01")));
        assertTrue(loanType.isAmountValid(new BigDecimal("499999.99")));
        assertFalse(loanType.isAmountValid(new BigDecimal("9999.99")));
        assertFalse(loanType.isAmountValid(new BigDecimal("500000.01")));
    }

    @Test
    @DisplayName("Builder pattern - all fields")
    void builderPatternAllFields() {
        // Arrange & Act
        LoanType loanType = LoanType.builder()
                .id("550e8400-e29b-41d4-a716-446655441003")
                .name("PERSONAL")
                .minimumAmount(new BigDecimal("20000"))
                .maximumAmount(new BigDecimal("800000"))
                .interestRate(new BigDecimal("15.0"))
                .automaticValidation(false)
                .build();

        // Assert
        assertEquals("550e8400-e29b-41d4-a716-446655441003", loanType.getId());
        assertEquals("PERSONAL", loanType.getName());
        assertEquals(new BigDecimal("20000"), loanType.getMinimumAmount());
        assertEquals(new BigDecimal("800000"), loanType.getMaximumAmount());
        assertEquals(new BigDecimal("15.0"), loanType.getInterestRate());
        assertFalse(loanType.getAutomaticValidation());
    }

    @Test
    @DisplayName("Edge case - minimum equals maximum")
    void edgeCaseMinimumEqualsMaximum() {
        // Arrange
        LoanType loanType = LoanType.builder()
                .id("550e8400-e29b-41d4-a716-446655441003")
                .name("FIXED_AMOUNT")
                .minimumAmount(new BigDecimal("100000"))
                .maximumAmount(new BigDecimal("100000"))
                .build();

        // Act & Assert
        assertTrue(loanType.isAmountValid(new BigDecimal("100000")));
        assertFalse(loanType.isAmountValid(new BigDecimal("99999")));
        assertFalse(loanType.isAmountValid(new BigDecimal("100001")));
    }
}