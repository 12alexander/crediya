package co.com.bancolombia.r2dbc.orders.mapper;

import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.r2dbc.orders.data.OrdersData;
import org.mapstruct.Mapper;

/**
 * 🎯 FIXED: MapStruct mapper for compile-time safe mapping
 * Eliminates manual mapping errors and improves performance
 */
@Mapper(componentModel = "spring")
public interface OrdersMapper {
    
    /**
     * Maps domain model to data entity
     */
    OrdersData toData(Orders orders);
    
    /**
     * Maps data entity to domain model  
     */
    Orders toDomain(OrdersData ordersData);
}
