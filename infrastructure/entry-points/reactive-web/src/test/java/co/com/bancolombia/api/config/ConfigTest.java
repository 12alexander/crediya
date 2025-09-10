package co.com.bancolombia.api.config;

import co.com.bancolombia.api.RouterRest;
import co.com.bancolombia.api.handler.OrderHandler;
import co.com.bancolombia.api.handler.ReportHandler;
import co.com.bancolombia.api.services.AuthServiceClient;
import co.com.bancolombia.api.util.ReportBuilder;
import co.com.bancolombia.transaction.TransactionalAdapter;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import jakarta.validation.Validator;

@ContextConfiguration(classes = {RouterRest.class, OrderHandler.class, ReportHandler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class, ValidationConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private IOrdersUseCase ordersUseCase;
    
    @MockBean
    private AuthServiceClient authServiceClient;
    
    @MockBean
    private Validator validator;
    
    @MockBean
    private ReportBuilder reportBuilder;
    
    @MockBean
    private TransactionalAdapter transactionalAdapter;

    @Test
    void testContextLoads() {
    }

}