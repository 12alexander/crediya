package co.com.bancolombia.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Simple test to verify router configuration loads correctly.
 */
class SimpleRouterTest {

    @Test
    void routerRestBasicTest() {
        RouterRest routerRest = new RouterRest();
        assertNotNull(routerRest);
    }
}