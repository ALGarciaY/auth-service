package co.com.bancolombia.api;

import co.com.bancolombia.api.errors.FunctionalErrorFilter;
import co.com.bancolombia.api.path.LoginPath;
import co.com.bancolombia.api.path.UsersPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RouterRestTest {

    @Mock private Handler handler;
    @Mock private LoginHandler loginHandler;
    @Mock private JwksHandler jwksHandler; // si RouterRest lo espera, mantenlo

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        FunctionalErrorFilter errorFilter = new FunctionalErrorFilter();
        UsersPath usersPath = new UsersPath();
        LoginPath loginPath = new LoginPath();

        RouterRest routerRest = new RouterRest(errorFilter, usersPath, loginPath, handler, loginHandler, jwksHandler);

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
