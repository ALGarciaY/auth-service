package co.com.bancolombia.api.config;

import co.com.bancolombia.api.errors.FunctionalErrorFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.codec.DecodingException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

class FunctionalErrorFilterTest {

    private FunctionalErrorFilter filter;
    private ServerRequest request;
    private HandlerFunction<ServerResponse> next;

    @BeforeEach
    void setUp() {
        filter = new FunctionalErrorFilter();

        // Mocks
        request = mock(ServerRequest.class);
        var exchange = mock(org.springframework.web.server.ServerWebExchange.class);
        var httpRequest = mock(org.springframework.http.server.reactive.ServerHttpRequest.class);

        when(request.path()).thenReturn("/test-path");
        when(request.exchange()).thenReturn(exchange);
        when(exchange.getRequest()).thenReturn(httpRequest);
        when(httpRequest.getId()).thenReturn("traceId-001");
    }

    @Test
    void testNormalFlow() {
        next = mock(HandlerFunction.class);

        when(next.handle(request)).thenReturn(ServerResponse.ok().build());

        StepVerifier.create(filter.filter(request, next))
                .expectNextCount(1)
                .verifyComplete();

        verify(next).handle(request);
    }

    @Test
    void testServerWebInputException() {
        ServerWebInputException ex = mock(ServerWebInputException.class);
        when(ex.getReason()).thenReturn("Invalid input");

        next = req -> Mono.error(ex);

        StepVerifier.create(filter.filter(request, next))
                .expectNextMatches(resp -> resp.statusCode().is4xxClientError())
                .verifyComplete();
    }

    @Test
    void testWebExchangeBindException() {
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        var fieldError = mock(org.springframework.validation.FieldError.class);
        when(fieldError.getField()).thenReturn("field");
        when(fieldError.getCode()).thenReturn("NotNull");
        when(fieldError.getDefaultMessage()).thenReturn("must not be null");
        when(ex.getFieldErrors()).thenReturn(List.of(fieldError));

        next = req -> Mono.error(ex);

        StepVerifier.create(filter.filter(request, next))
                .expectNextMatches(resp -> resp.statusCode().is4xxClientError())
                .verifyComplete();
    }

    @Test
    void testDecodingException() {
        DecodingException ex = mock(DecodingException.class);

        next = req -> Mono.error(ex);

        StepVerifier.create(filter.filter(request, next))
                .expectNextMatches(resp -> resp.statusCode().is4xxClientError())
                .verifyComplete();
    }

    @Test
    void testIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Conflict happened");

        next = req -> Mono.error(ex);

        StepVerifier.create(filter.filter(request, next))
                .expectNextMatches(resp -> resp.statusCode().value() == 409)
                .verifyComplete();
    }

    @Test
    void testGenericThrowable() {
        RuntimeException ex = new RuntimeException("Unexpected");

        next = req -> Mono.error(ex);

        StepVerifier.create(filter.filter(request, next))
                .expectNextMatches(resp -> resp.statusCode().is5xxServerError())
                .verifyComplete();
    }
}
