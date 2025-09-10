package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
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

    private final UserEntity userEntity = new UserEntity(
            userId.toString(),
            "John",
            "Doe",
            "123456789",
            "john.doe@test.com",
            BigDecimal.valueOf(3000),
            LocalDate.of(1990, 1, 1),
            "123 Street",
            "1234567890",
            1,
            "hashedpassword",
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
        when(repository.findById(userId.toString())).thenReturn(Mono.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        Mono<User> result = repositoryAdapter.findById(userId.toString());

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> foundUser.getId().equals(userId.toString()))
                .verifyComplete();
    }

    @Test
    void mustFindAllValues() {
        when(repository.findAll()).thenReturn(Flux.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        Flux<User> result = repositoryAdapter.findAll();

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> foundUser.getEmail().equals("john.doe@test.com"))
                .verifyComplete();
    }

    @Test
    void mustSaveValue() {
        when(mapper.map(user, UserEntity.class)).thenReturn(userEntity); // domain -> entity
        when(repository.save(userEntity)).thenReturn(Mono.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);     // entity -> domain

        Mono<User> result = repositoryAdapter.save(user);

        StepVerifier.create(result)
                .expectNextMatches(savedUser -> savedUser.getEmail().equals("john.doe@test.com"))
                .verifyComplete();
    }

    @Test
    void mustCheckIfEmailExists() {
        String email = "john.doe@test.com";
        when(repository.existsByEmail(email)).thenReturn(Mono.just(true));

        Mono<Boolean> result = repositoryAdapter.existsByEmail(email);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }
}
