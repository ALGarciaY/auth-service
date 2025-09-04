package co.com.bancolombia.api.config;

import co.com.bancolombia.api.Handler;
import co.com.bancolombia.api.RouterRest;
import co.com.bancolombia.api.errors.FunctionalErrorFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ConfigTest {

    @Mock
    private Handler handler;

    @InjectMocks
    private RouterRest routerRest;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // ⬇⬇⬇ CAMBIO: ahora RouterRest recibe filtro, paths y handler
        routerRest = new RouterRest(new FunctionalErrorFilter(), new UsersPath(), handler);

        // ⬇⬇⬇ CAMBIO: routerFunction() ya NO recibe handler por parámetro
        RouterFunction<ServerResponse> routerFunction = routerRest.routerFunction();
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void createUserRoute_shouldReturnOk() {
        when(handler.createUser(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        webTestClient.post()
                .uri("/api/v1/usuarios/createUser")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getUserByIdRoute_shouldReturnOk() {
        when(handler.getUserById(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        webTestClient.get()
                .uri("/api/v1/usuarios/getUserById/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getAllUsersRoute_shouldReturnOk() {
        when(handler.getAllUsers(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        webTestClient.get()
                .uri("/api/v1/usuarios/getAllUsers")
                .exchange()
                .expectStatus().isOk();
    }
}
