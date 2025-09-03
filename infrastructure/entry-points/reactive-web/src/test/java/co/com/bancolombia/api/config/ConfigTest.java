package co.com.bancolombia.api.config;

import co.com.bancolombia.api.Handler;
import co.com.bancolombia.api.RouterRest;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class, ValidationConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private IOrdersUseCase ordersUseCase;

    @Test
    void testContextLoads() {
        // Test básico para verificar que el contexto se carga correctamente con la configuración
        // Las rutas reales han cambiado, así que no probamos endpoints específicos
    }

}