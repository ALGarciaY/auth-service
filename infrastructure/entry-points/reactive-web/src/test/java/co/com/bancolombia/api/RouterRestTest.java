package co.com.bancolombia.api;

import co.com.bancolombia.api.errors.FunctionalErrorFilter;
import co.com.bancolombia.api.config.UsersPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RouterRestTest {

    private Handler handler;
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        handler = mock(Handler.class);

        FunctionalErrorFilter errorFilter = new FunctionalErrorFilter();

        // ⬇⬇⬇ CAMBIO: se crea UsersPath y se inyecta junto con el handler
        UsersPath usersPath = new UsersPath();
        RouterRest routerRest = new RouterRest(errorFilter, usersPath, handler);

        // ⬇⬇⬇ CAMBIO: routerFunction() ya NO recibe handler por parámetro
        RouterFunction<ServerResponse> routerFunction = routerRest.routerFunction();
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void testCreateUserRoute() {
        when(handler.createUser(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        webTestClient.post()
                .uri("/api/v1/usuarios/createUser")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetUserByIdRoute() {
        when(handler.getUserById(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        webTestClient.get()
                .uri("/api/v1/usuarios/getUserById/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetAllUsersRoute() {
        when(handler.getAllUsers(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        webTestClient.get()
                .uri("/api/v1/usuarios/getAllUsers")
                .exchange()
                .expectStatus().isOk();
    }
}
