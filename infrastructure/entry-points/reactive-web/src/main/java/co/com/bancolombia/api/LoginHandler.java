package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.request.LoginRequest;
import co.com.bancolombia.usecase.user.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class LoginHandler {
    private final AuthUseCase authUseCase;

    public Mono<ServerResponse> login(ServerRequest req) {
        return req.bodyToMono(LoginRequest.class)
                .flatMap(b -> authUseCase.login(b.getEmail(), b.getPassword()))
                .flatMap(token -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(token));
    }
}
