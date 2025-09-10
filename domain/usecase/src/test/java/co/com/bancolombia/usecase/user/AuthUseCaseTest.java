package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.AuthToken;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.errors.BusinessError;
import co.com.bancolombia.model.user.errors.BusinessException;
import co.com.bancolombia.model.user.gateways.JwtSigner;
import co.com.bancolombia.model.user.gateways.PasswordHasher;
import co.com.bancolombia.model.user.gateways.RoleRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock private UserRepository repository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordHasher passwordHasher;
    @Mock private JwtSigner jwtSigner;

    private AuthUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthUseCase(repository, roleRepository, passwordHasher, jwtSigner);
    }

    private User user() {
        return User.builder()
                .id("123")
                .email("john@doe.com")
                .password("$2a$10$hash")
                .roleId(1)
                .build();
    }

    @Test
    void login_shouldFail_whenUserNotFound() {
        when(repository.findByEmail("x@y.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.login("x@y.com", "pwd"))
                .expectErrorSatisfies(t -> {
                    BusinessException ex = (BusinessException) t;
                    org.junit.jupiter.api.Assertions.assertEquals(BusinessError.RECORDS_NOT_FOUND, ex.getError());
                })
                .verify();

        verify(passwordHasher, never()).matches(anyString(), anyString());
        verify(jwtSigner, never()).sign(any());
    }

    @Test
    void login_shouldFail_whenPasswordInvalid() {
        when(repository.findByEmail(user().getEmail())).thenReturn(Mono.just(user()));
        when(passwordHasher.matches("raw", user().getPassword())).thenReturn(false);

        StepVerifier.create(useCase.login(user().getEmail(), "raw"))
                .expectErrorSatisfies(t -> {
                    BusinessException ex = (BusinessException) t;
                    assertEquals(BusinessError.UNAUTHORIZED, ex.getError());
                })
                .verify();

        verify(jwtSigner, never()).sign(any());
    }

    @Test
    void login_shouldReturnToken_whenCredentialsOk() {
        User db = user();
        when(repository.findByEmail(db.getEmail())).thenReturn(Mono.just(db));
        when(passwordHasher.matches("raw", db.getPassword())).thenReturn(true);
        when(roleRepository.findNameById(db.getRoleId())).thenReturn(Mono.just("USER"));
        when(jwtSigner.sign(any(User.class))).thenReturn(Mono.just(
                AuthToken.builder()
                        .token("jwt")
                        .role("USER")
                        .email(db.getEmail())
                        .expiresInSeconds(3600)
                        .build()
        ));

        StepVerifier.create(useCase.login(db.getEmail(), "raw"))
                .assertNext(tok -> {
                    org.junit.jupiter.api.Assertions.assertEquals("jwt", tok.token());
                    org.junit.jupiter.api.Assertions.assertEquals("USER", tok.role());
                    org.junit.jupiter.api.Assertions.assertEquals(db.getEmail(), tok.email());
                })
                .verifyComplete();
    }
}
