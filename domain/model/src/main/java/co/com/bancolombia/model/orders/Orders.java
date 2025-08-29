package co.com.bancolombia.model.orders;
import lombok.*;
//import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Orders {
    private String id;
    private BigDecimal amount;
    private Integer deadline;
    private String emailAddress;
    private String idEstado;
    private String idTipoPrestamo;

    public static class OrdersBuilder {
        public OrdersBuilder amount(BigDecimal amount) {
            if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
                throw new IllegalArgumentException("El Monto, tiene que ser mayor que 0");
            }
            return this;
        }

        public OrdersBuilder deadline(Integer deadline) {
            if(deadline == null || deadline <= 0){
                throw new IllegalArgumentException("El Plazo, tiene que ser mayor que 0");
            }
            return this;
        }

        public OrdersBuilder emailAddress(String emailAddress) {
            if(emailAddress == null || emailAddress.trim().isEmpty()){
                throw new IllegalArgumentException("Email no puede ser Nulo o Vacio");
            }
            String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
            if(!emailAddress.matches(emailRegex)){
                throw new IllegalArgumentException("Email no cumple con el formato ");
            }
            this.emailAddress = emailAddress;
            return this;
        }

        public OrdersBuilder idTipoPrestamo(String idTipoPrestamo) {
            if(idTipoPrestamo == null || idTipoPrestamo.trim().isEmpty()){
                throw new IllegalArgumentException("Tipo de Prestamo no puede ser Nulo o Vacio");
            }
            this.idTipoPrestamo = idTipoPrestamo.trim();
            return this;
        }
    }

    public void  validateData(){
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El Monto, tiene que ser mayor que 0");
        }

        if (deadline == null || deadline <= 0) {
            throw new IllegalArgumentException("Los apellidos no pueden ser nulos o vacíos");
        }

        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no puede ser nulo o vacío");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!emailAddress.matches(emailRegex)) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido");
        }

        if (idTipoPrestamo == null || idTipoPrestamo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de Prestamo no puede ser Nulo o Vacio");
        }

    }

}
