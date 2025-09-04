package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.errors.BusinessError;
import co.com.bancolombia.model.user.errors.BusinessException;
import co.com.bancolombia.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
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
    void createUser_shouldReturnBusinessException_whenEmailExists() {
        // Arrange
        User user = buildSampleUser();
        when(repository.existsByEmail(user.getEmail())).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(useCase.createUser(user))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof BusinessException);
                    BusinessException ex = (BusinessException) throwable;
                    assertEquals(BusinessError.EMAIL_ALREADY_EXISTS, ex.getError());
                    // Mensaje exacto si en tu use case usas override con el correo:
                    assertEquals("El correo %s ya se encuentra registrado."
                                    .formatted(user.getEmail()),
                            ex.getMessage());
                })
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
    void getUserById_shouldReturnBusinessException_whenNotFound() {
        // Arrange
        String id = "not_found";
        when(repository.findById(id)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.getUserById(id))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof BusinessException);
                    BusinessException ex = (BusinessException) throwable;
                    assertEquals(BusinessError.RECORDS_NOT_FOUND, ex.getError());
                    assertEquals("Usuario no encontrado", ex.getMessage());
                })
                .verify();
    }

    @Test
    void getAllUsers_shouldReturnBusinessException_whenNoUsersExist() {
        when(repository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.getAllUsers())
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof BusinessException);
                    BusinessException ex = (BusinessException) throwable;
                    assertEquals(BusinessError.USERS_NOT_FOUND, ex.getError());
                    assertEquals("No hay usuarios registrados", ex.getMessage());
                })
                .verify();

        verify(repository).findAll();
    }

    private User buildSampleUser() {
        return User.builder()
                .id("123")
                .firstName("John")
                .lastName("Doe")
                .email("johne.doe@example.com")
                .baseSalary(BigDecimal.valueOf(5_000_000))
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .phone("5551234567")
                .build();
    }
}
