package co.com.bancolombia.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLoanRequestDTO {
    
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que 0")
    @Digits(integer = 13, fraction = 2, message = "El monto no puede tener más de 2 decimales")
    private BigDecimal amount;
    
    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo debe ser mayor que 0")
    @Max(value = 360, message = "El plazo no puede ser mayor a 360 meses")
    private Integer deadline;
    
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @JsonProperty("email_address")
    private String emailAddress;
    
    @NotBlank(message = "El tipo de préstamo es obligatorio")
    @JsonProperty("id_tipo_prestamo")
    private String loanTypeId;
}