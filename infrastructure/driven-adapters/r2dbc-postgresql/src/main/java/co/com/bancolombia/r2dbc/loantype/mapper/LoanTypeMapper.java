package co.com.bancolombia.r2dbc.loantype.mapper;

import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.r2dbc.loantype.data.LoanTypeData;
import org.mapstruct.Mapper;

/**
 * 🎯 FIXED: MapStruct mapper for compile-time safe mapping
 * Eliminates manual mapping errors and improves performance
 */
@Mapper(componentModel = "spring")
public interface LoanTypeMapper {
    
    /**
     * Maps data entity to domain model
     */
    LoanType toDomain(LoanTypeData loanTypeData);
    
    /**
     * Maps domain model to data entity
     */
    LoanTypeData toData(LoanType loanType);
}