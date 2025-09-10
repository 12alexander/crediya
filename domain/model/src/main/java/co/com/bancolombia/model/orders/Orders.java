package co.com.bancolombia.model.orders;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Orders {
    private String id;
    private BigDecimal amount;
    private Integer deadline;
    private String emailAddress;
    private String idStatus;
    private String idLoanType;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    public static Orders createNew(BigDecimal amount, Integer deadline, 
                                 String emailAddress, String idLoanType, String pendingStatusId) {
        LocalDateTime now = LocalDateTime.now();
        return Orders.builder()
                .id(UUID.randomUUID().toString())
                .amount(amount)
                .deadline(deadline)
                .emailAddress(emailAddress)
                .idLoanType(idLoanType)
                .idStatus(pendingStatusId)
                .creationDate(now)
                .updateDate(now)
                .build();
    }

    public void validateForCreation() {
        validateAmount();
        validateDeadline();
        validateEmailAddress();
        validateLoanType();
    }

    private void validateAmount() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que 0");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("El monto no puede tener más de 2 decimales");
        }
    }

    private void validateDeadline() {
        if (deadline == null || deadline <= 0) {
            throw new IllegalArgumentException("El plazo debe ser mayor que 0");
        }
        if (deadline > 360) {
            throw new IllegalArgumentException("El plazo no puede ser mayor a 360 meses");
        }
    }

    private void validateEmailAddress() {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!emailAddress.matches(emailRegex)) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido");
        }
    }

    private void validateLoanType() {
        if (idLoanType == null || idLoanType.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de préstamo es obligatorio");
        }
    }
}
