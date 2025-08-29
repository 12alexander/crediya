package co.com.bancolombia.r2dbc.orders.mapper;

import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.r2dbc.orders.data.OrdersData;

import java.time.LocalDateTime;

public class OrdersMapper {
    private OrdersMapper(){
        throw new IllegalStateException("Utility class");
    }
    public static OrdersData toDataForCreation(Orders orders){
        LocalDateTime now = LocalDateTime.now();
        return OrdersData.builder()
                .id(orders.getId())
                .amount(orders.getAmount())
                .deadline(orders.getDeadline())
                .emailAddress(orders.getEmailAddress())
                .idEstado(orders.getIdEstado())
                .idTipoPrestamo(orders.getIdTipoPrestamo())
                .creationDate(now)
                .updateDate(now)
                .build();
    }

    public static OrdersData toDataForUpdate(Orders orders){
        LocalDateTime now = LocalDateTime.now();
        return OrdersData.builder()
                .id(orders.getId())
                .amount(orders.getAmount())
                .deadline(orders.getDeadline())
                .emailAddress(orders.getEmailAddress())
                .idEstado(orders.getIdEstado())
                .idTipoPrestamo(orders.getIdTipoPrestamo())
                .creationDate(now)
                .updateDate(now)
                .build();
    }

    public static Orders toDomain(OrdersData ordersData){
        return Orders.builder()
                .id(ordersData.getId())
                .amount(ordersData.getAmount())
                .deadline(ordersData.getDeadline())
                .emailAddress(ordersData.getEmailAddress())
                .idEstado(ordersData.getIdEstado())
                .idTipoPrestamo(ordersData.getIdTipoPrestamo())
                .build();
    }

}
