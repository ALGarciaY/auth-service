package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.DtoValidator;
import co.com.bancolombia.api.dto.request.CreateUserRequest;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.usecase.user.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HandlerTest {

    @Mock
    private UserUseCase useCase;

    @Mock
    private DtoValidator validator;

    @InjectMocks
    private Handler handler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private User buildUser(String id) {
        return User.builder()
                .id(id)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane" + id + "@example.com")
                .baseSalary(BigDecimal.valueOf(60000))
                .birthDate(LocalDate.of(1992, 5, 10))
                .address("Somewhere")
                .phone("789456")
                .build();
    }

    private CreateUserRequest buildCreateUserRequest() {
        CreateUserRequest req = new CreateUserRequest();
        req.setFirstName("John");
        req.setLastName("Smith");
        req.setEmail("john.smith@example.com");
        req.setBaseSalary(BigDecimal.valueOf(45000));
        req.setBirthDate(LocalDate.of(1990, 1, 1));
        req.setAddress("Address");
        req.setPhone("1234567890");
        return req;
    }

    @Test
    void createUser_shouldReturnCreatedResponse() {
        CreateUserRequest requestBody = buildCreateUserRequest();
        User userDomain = buildUser("1");

        when(validator.validate(any(CreateUserRequest.class))).thenReturn(Mono.just(requestBody));
        when(useCase.createUser(any(User.class))).thenReturn(Mono.just(userDomain));

        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(CreateUserRequest.class)).thenReturn(Mono.just(requestBody));
        when(request.path()).thenReturn("/api/v1/usuarios/createUser");

        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/usuarios/createUser").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);
        when(request.exchange()).thenReturn(exchange);

        Mono<ServerResponse> responseMono = handler.createUser(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> serverResponse.statusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    void getUserById_shouldReturnUserResponse() {
        User user = buildUser("1");

        when(useCase.getUserById("1")).thenReturn(Mono.just(user));

        ServerRequest request = mock(ServerRequest.class);
        when(request.pathVariable("id")).thenReturn("1");
        when(request.path()).thenReturn("/api/v1/usuarios/getUserById/1");

        MockServerHttpRequest httpRequest = MockServerHttpRequest.get("/api/v1/usuarios/getUserById/1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);
        when(request.exchange()).thenReturn(exchange);

        Mono<ServerResponse> responseMono = handler.getUserById(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> serverResponse.statusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    void getAllUsers_shouldReturnFluxOfUsers() {
        User user1 = buildUser("1");
        User user2 = buildUser("2");

        when(useCase.getAllUsers()).thenReturn(Flux.just(user1, user2));

        ServerRequest request = mock(ServerRequest.class);
        when(request.path()).thenReturn("/api/v1/usuarios/getAllUsers");

        MockServerHttpRequest httpRequest = MockServerHttpRequest.get("/api/v1/usuarios/getAllUsers").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(httpRequest);
        when(request.exchange()).thenReturn(exchange);

        Mono<ServerResponse> responseMono = handler.getAllUsers(request);

        StepVerifier.create(responseMono)
                .expectNextMatches(serverResponse -> serverResponse.statusCode().is2xxSuccessful())
                .verifyComplete();
    }
}
