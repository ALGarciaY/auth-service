package co.com.bancolombia.api;

import co.com.bancolombia.model.user.AuthToken;
import co.com.bancolombia.usecase.user.AuthUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginHandlerTest {

    @Mock
    private AuthUseCase authUseCase;

    private LoginHandler handler; // real
    private WebTestClient web;

    @BeforeEach
    void setUp() {
        handler = new LoginHandler(authUseCase);

        RouterFunction<ServerResponse> routes = RouterFunctions.route()
                .POST("/login", handler::login)
                .build();

        web = WebTestClient.bindToRouterFunction(routes).build();
    }

    @Test
    void login_ok() {
        var token = AuthToken.builder()
                .token("jwt")
                .email("john@doe.com")
                .role("USER")
                .expiresInSeconds(3600)
                .build();

        when(authUseCase.login(eq("john@doe.com"), eq("pwd")))
                .thenReturn(Mono.just(token));

        web.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"email\":\"john@doe.com\",\"password\":\"pwd\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.token").isEqualTo("jwt")
                .jsonPath("$.email").isEqualTo("john@doe.com")
                .jsonPath("$.role").isEqualTo("USER");
    }
}
