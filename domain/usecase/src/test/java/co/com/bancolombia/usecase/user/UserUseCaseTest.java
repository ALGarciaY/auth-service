package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.errors.BusinessError;
import co.com.bancolombia.model.user.errors.BusinessException;
import co.com.bancolombia.model.user.gateways.PasswordHasher;
import co.com.bancolombia.model.user.gateways.RoleRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock private UserRepository repository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordHasher passwordHasher;

    private UserUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new UserUseCase(repository, roleRepository, passwordHasher);
    }

    @Test
    void createUser_shouldSaveUser_whenEmailNotExists() {
        User user = buildSampleUser();
        when(roleRepository.existsById(user.getRoleId())).thenReturn(Mono.just(true));

        when(repository.existsByEmail(user.getEmail())).thenReturn(Mono.just(false));

        when(passwordHasher.encode(anyString())).thenReturn("hashed-pass");

        when(repository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.createUser(user))
                .assertNext(u -> {
                    assertEquals(user.getEmail(), u.getEmail());
                })
                .verifyComplete();

        verify(roleRepository).existsById(user.getRoleId());
        verify(repository).existsByEmail(user.getEmail());
        verify(passwordHasher).encode(anyString());
        verify(repository).save(any(User.class));
    }

    @Test
    void createUser_shouldReturnBusinessException_whenEmailExists() {
        User user = buildSampleUser();

        when(roleRepository.existsById(user.getRoleId())).thenReturn(Mono.just(true));

        when(repository.existsByEmail(user.getEmail())).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.createUser(user))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof BusinessException);
                    BusinessException ex = (BusinessException) throwable;
                    assertEquals(BusinessError.EMAIL_ALREADY_EXISTS, ex.getError());
                    assertEquals(("El correo %s ya se encuentra registrado.").formatted(user.getEmail()), ex.getMessage());
                })
                .verify();

        verify(roleRepository).existsById(user.getRoleId());
        verify(repository).existsByEmail(user.getEmail());
        verify(repository, never()).save(any());
        verify(passwordHasher, never()).encode(anyString());
    }

    @Test
    void getUserById_shouldReturnUser_whenFound() {
        User user = buildSampleUser();
        when(repository.findById(user.getId())).thenReturn(Mono.just(user));

        StepVerifier.create(useCase.getUserById(user.getId()))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void getUserById_shouldReturnBusinessException_whenNotFound() {
        String id = "not_found";
        when(repository.findById(id)).thenReturn(Mono.empty());

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

    @Test
    void createUser_shouldFail_whenRoleDoesNotExist() {
        User user = buildSampleUser();
        when(roleRepository.existsById(user.getRoleId())).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.createUser(user))
                .expectErrorSatisfies(t -> {
                    BusinessException ex = (BusinessException) t;
                    assertEquals(BusinessError.ROLE_NOT_FOUND, ex.getError());
                })
                .verify();

        verify(repository, never()).existsByEmail(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void createUser_shouldNotReencode_whenPasswordLooksBcrypt() {
        User user = buildSampleUser().toBuilder()
                .password("$2a$10$abcdefghijklmnopqrstuvxyzabcdefghi1234567890") // simula bcrypt
                .build();

        when(roleRepository.existsById(user.getRoleId())).thenReturn(Mono.just(true));
        when(repository.existsByEmail(user.getEmail())).thenReturn(Mono.just(false));
        when(repository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.createUser(user))
                .assertNext(saved -> assertEquals(user.getPassword(), saved.getPassword()))
                .verifyComplete();

        verify(passwordHasher, never()).encode(anyString());
    }

    @Test
    void createUser_shouldUseDocumentAsPassword_whenPasswordBlank() {
        User user = buildSampleUser().toBuilder()
                .password("   ") // blanco
                .document("1010")
                .build();

        when(roleRepository.existsById(user.getRoleId())).thenReturn(Mono.just(true));
        when(repository.existsByEmail(user.getEmail())).thenReturn(Mono.just(false));
        when(passwordHasher.encode("1010")).thenReturn("hashed-1010");
        when(repository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.createUser(user))
                .assertNext(saved -> assertEquals("hashed-1010", saved.getPassword()))
                .verifyComplete();
    }

    @Test
    void getUserByDocument_shouldReturnUser_whenFound() {
        User user = buildSampleUser();
        when(repository.findByDocument(user.getDocument())).thenReturn(Mono.just(user));

        StepVerifier.create(useCase.getUserByDocument(user.getDocument()))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void getUserByDocument_shouldFail_whenNotFound() {
        when(repository.findByDocument("X")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getUserByDocument("X"))
                .expectErrorSatisfies(t -> assertEquals(BusinessError.RECORDS_NOT_FOUND, ((BusinessException)t).getError()))
                .verify();
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenFound() {
        User user = buildSampleUser();
        when(repository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));

        StepVerifier.create(useCase.getUserByEmail(user.getEmail()))
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void getUserByEmail_shouldFail_whenNotFound() {
        when(repository.findByEmail("a@b.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getUserByEmail("a@b.com"))
                .expectErrorSatisfies(t -> assertEquals(BusinessError.RECORDS_NOT_FOUND, ((BusinessException)t).getError()))
                .verify();
    }

    @Test
    void getAllUsers_shouldReturnFlux_whenThereAreUsers() {
        User u1 = buildSampleUser();
        User u2 = buildSampleUser().toBuilder().id("456").email("jane.doe@example.com").build();
        when(repository.findAll()).thenReturn(Flux.just(u1, u2));

        StepVerifier.create(useCase.getAllUsers())
                .expectNext(u1, u2)
                .verifyComplete();
    }

    private User buildSampleUser() {
        return User.builder()
                .id("123")
                .firstName("John")
                .lastName("Doe")
                .email("johne.doe@example.com")
                .password("plain-pass")
                .document("10101010")
                .roleId(1)
                .roleName("USER")
                .baseSalary(BigDecimal.valueOf(5_000_000))
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .phone("5551234567")
                .build();
    }
}
