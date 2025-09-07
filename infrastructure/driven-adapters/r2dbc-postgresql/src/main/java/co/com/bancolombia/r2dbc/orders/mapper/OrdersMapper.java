package co.com.bancolombia.r2dbc.orders.mapper;

import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.r2dbc.orders.data.OrdersData;

import java.time.LocalDateTime;

public class OrdersMapper {
    private OrdersMapper(){
        throw new IllegalStateException("Utility class");
    }
    public static OrdersData toData(Orders orders){
        return OrdersData.builder()
                .id(orders.getId())
                .documentId(orders.getDocumentId())
                .amount(orders.getAmount())
                .deadline(orders.getDeadline())
                .emailAddress(orders.getEmailAddress())
                .idStatus(orders.getIdStatus())
                .idLoanType(orders.getIdLoanType())
                .creationDate(orders.getCreationDate())
                .updateDate(orders.getUpdateDate())
                .build();
    }

    public static Orders toDomain(OrdersData ordersData){
        return Orders.builder()
                .id(ordersData.getId())
                .documentId(ordersData.getDocumentId())
                .amount(ordersData.getAmount())
                .deadline(ordersData.getDeadline())
                .emailAddress(ordersData.getEmailAddress())
                .idStatus(ordersData.getIdStatus())
                .idLoanType(ordersData.getIdLoanType())
                .creationDate(ordersData.getCreationDate())
                .updateDate(ordersData.getUpdateDate())
                .build();
    }

}
