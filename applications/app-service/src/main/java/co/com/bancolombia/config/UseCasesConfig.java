package co.com.bancolombia.config;

import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.orders.gateways.OrdersRepository;
import co.com.bancolombia.usecase.orders.OrdersUseCase;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    /**
     * Creates the Orders use case bean.
     * 
     * @param ordersRepository repository for order operations
     * @param loanTypeRepository repository for loan type operations
     * @return IOrdersUseCase implementation
     */
    @Bean
    public IOrdersUseCase ordersUseCase(OrdersRepository ordersRepository, LoanTypeRepository loanTypeRepository) {
        return new OrdersUseCase(ordersRepository, loanTypeRepository);
    }
}
