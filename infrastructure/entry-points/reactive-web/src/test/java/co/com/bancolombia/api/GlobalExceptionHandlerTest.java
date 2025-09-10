package co.com.bancolombia.api;

import co.com.bancolombia.api.config.GlobalExceptionHandler;
import co.com.bancolombia.model.orders.exceptions.OrdersBusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.UnexpectedTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.*;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    private String getResponseBody(ServerHttpResponse response) {
        if (response instanceof MockServerHttpResponse mockResponse) {
            return mockResponse.getBodyAsString().block();
        }
        return null;
    }

    @Test
    @DisplayName("Should handle OrdersBusinessException with NOT_FOUND")
    void handleOrdersBusinessException() {
        OrdersBusinessException ex = new OrdersBusinessException("ORDER_NOT_FOUND", "Order not found");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(404);
        assertThat(getResponseBody(exchange.getResponse())).contains("ORDER_NOT_FOUND");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException as BAD_REQUEST")
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad arg");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(400);
        assertThat(getResponseBody(exchange.getResponse())).contains("INVALID_ARGUMENT");
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException as BAD_REQUEST")
    void handleConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("must not be null");
        when(violation.getPropertyPath()).thenReturn(mock(Path.class));
        when(violation.getMessageTemplate()).thenReturn("{NotNull}");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(400);
        assertThat(getResponseBody(exchange.getResponse())).contains("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("Should handle UnexpectedTypeException as BAD_REQUEST")
    void handleUnexpectedTypeException() {
        UnexpectedTypeException ex = new UnexpectedTypeException("wrong type");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(400);
        assertThat(getResponseBody(exchange.getResponse())).contains("VALIDATION_TYPE_ERROR");
    }

    @Test
    @DisplayName("Should handle ServerWebInputException as BAD_REQUEST")
    void handleServerWebInputException() {
        ServerWebInputException ex = new ServerWebInputException("bad body");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(400);
        assertThat(getResponseBody(exchange.getResponse())).contains("INVALID_REQUEST_BODY");
    }

    @Test
    @DisplayName("Should handle DecodingException as BAD_REQUEST")
    void handleDecodingException() {
        org.springframework.core.codec.DecodingException ex =
                new org.springframework.core.codec.DecodingException("json error");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(400);
        assertThat(getResponseBody(exchange.getResponse())).contains("JSON_DECODE_ERROR");
    }

    @Test
    @DisplayName("Should handle UnsupportedMediaTypeStatusException as 415")
    void handleUnsupportedMediaType() {
        UnsupportedMediaTypeStatusException ex = new UnsupportedMediaTypeStatusException("application/xml");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(415);
        assertThat(getResponseBody(exchange.getResponse())).contains("UNSUPPORTED_MEDIA_TYPE");
    }

    @Test
    @DisplayName("Should handle NotAcceptableStatusException as 406")
    void handleNotAcceptableStatusException() {
        NotAcceptableStatusException ex = new NotAcceptableStatusException("not acceptable");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(406);
        assertThat(getResponseBody(exchange.getResponse())).contains("NOT_ACCEPTABLE");
    }

    @Test
    @DisplayName("Should handle generic Exception as INTERNAL_SERVER_ERROR")
    void handleGenericException() {
        RuntimeException ex = new RuntimeException("unexpected");
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        handler.handle(exchange, ex).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(500);
        assertThat(getResponseBody(exchange.getResponse())).contains("INTERNAL_ERROR");
    }
}
