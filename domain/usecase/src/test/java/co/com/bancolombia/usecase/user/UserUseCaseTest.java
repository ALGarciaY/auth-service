package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

class UserUseCaseTest {

    private UserRepository repository;
    private UserUseCase useCase;

    @BeforeEach
    void setup() {
        repository = mock(UserRepository.class);
        useCase = new UserUseCase(repository);
    }

    @Test
    void createUser_shouldSaveUser_whenEmailNotExists() {
        // Arrange
        User user = buildSampleUser();

        when(repository.existsByEmail(user.getEmail())).thenReturn(Mono.just(false));
        when(repository.save(user)).thenReturn(Mono.just(user));

        // Act & Assert
        StepVerifier.create(useCase.createUser(user))
                .expectNext(user)
                .verifyComplete();

        verify(repository).save(user);
    }

    @Test
    void createUser_shouldReturnError_whenEmailExists() {
        // Arrange
        User user = buildSampleUser();

        when(repository.existsByEmail(user.getEmail())).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(useCase.createUser(user))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().equals("correo_electronico ya registrado"))
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void getUserById_shouldReturnUser_whenFound() {
        // Arrange
        User user = buildSampleUser();
        String id = user.getId();

        when(repository.findById(id)).thenReturn(Mono.just(user));

        // Act & Assert
        StepVerifier.create(useCase.getUserById(id))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void getUserById_shouldReturnError_whenNotFound() {
        // Arrange
        String id = "not_found";

        when(repository.findById(id)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.getUserById(id))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalStateException &&
                                throwable.getMessage().equals("Usuario no encontrado"))
                .verify();
    }

    private User buildSampleUser() {
        return User.builder()
                .id("123")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .baseSalary(BigDecimal.valueOf(5000000))
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .phone("5551234567")
                .build();
    }
}
