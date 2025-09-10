package co.com.bancolombia.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequestResponseDTO {
    
    private String id;
    
    private BigDecimal amount;
    
    private Integer deadline;
    
    @JsonProperty("email_address")
    private String emailAddress;
    
    private String status;
    
    @JsonProperty("tipo_prestamo")
    private String loanType;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("fecha_creacion")
    private LocalDateTime creationDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("fecha_actualizacion")
    private LocalDateTime updateDate;
}