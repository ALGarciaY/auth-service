package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.r2dbc.data.UserData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyReactiveRepositoryAdapterTest {

    @InjectMocks
    MyReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    MyReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private final UUID userId = UUID.randomUUID();

    private final UserData userData = new UserData(
            userId.toString(),
            "John",
            "Doe",
            "john.doe@test.com",
            BigDecimal.valueOf(3000),
            LocalDate.of(1990, 1, 1),
            "123 Street",
            "1234567890",
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    private final User user = User.builder()
            .id(userId.toString())
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@test.com")
            .baseSalary(BigDecimal.valueOf(3000))
            .birthDate(LocalDate.of(1990, 1, 1))
            .address("123 Street")
            .phone("1234567890")
            .build();

    @Test
    void mustFindValueById() {
        when(repository.findById(userId.toString())).thenReturn(Mono.just(userData));
        when(mapper.map(userData, User.class)).thenReturn(user);

        Mono<User> result = repositoryAdapter.findById(userId.toString());

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> foundUser.getId().equals(userId.toString()))
                .verifyComplete();
    }

    @Test
    void mustFindAllValues() {
        when(repository.findAll()).thenReturn(Flux.just(userData));
        when(mapper.map(userData, User.class)).thenReturn(user);

        Flux<User> result = repositoryAdapter.findAll();

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> foundUser.getEmail().equals("john.doe@test.com"))
                .verifyComplete();
    }

    @Test
    void mustSaveValue() {
        when(mapper.map(user, UserData.class)).thenReturn(userData); // domain -> entity
        when(repository.save(userData)).thenReturn(Mono.just(userData));
        when(mapper.map(userData, User.class)).thenReturn(user);     // entity -> domain

        Mono<User> result = repositoryAdapter.save(user);

        StepVerifier.create(result)
                .expectNextMatches(savedUser -> savedUser.getEmail().equals("john.doe@test.com"))
                .verifyComplete();
    }
}
