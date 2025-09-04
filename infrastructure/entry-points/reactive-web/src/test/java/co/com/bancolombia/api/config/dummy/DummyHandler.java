package co.com.bancolombia.api.config.dummy;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class DummyHandler {

    public Mono<ServerResponse> ok(ServerRequest request) {
        return ServerResponse.ok().bodyValue("ok");
    }

    public Mono<ServerResponse> throwError(ServerRequest request) {
        return ServerResponse.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("message", "correo electronico ya registrado"));
    }
}

