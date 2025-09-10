package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.AuthToken;
import co.com.bancolombia.model.user.errors.BusinessError;
import co.com.bancolombia.model.user.errors.BusinessException;
import co.com.bancolombia.model.user.gateways.JwtSigner;
import co.com.bancolombia.model.user.gateways.PasswordHasher;
import co.com.bancolombia.model.user.gateways.RoleRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthUseCase {
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final JwtSigner jwtSigner;

    public Mono<AuthToken> login(String email, String rawPassword) {
        return repository.findByEmail(email)
                .switchIfEmpty(Mono.error(
                        new BusinessException(BusinessError.RECORDS_NOT_FOUND, "Usuario/clave invalidos")))
                .flatMap(user -> {
                    boolean ok = passwordHasher.matches(rawPassword, user.getPassword());
                    if (!ok) {
                        return Mono.error(new BusinessException(BusinessError.UNAUTHORIZED, "Usuario/clave invalidos"));
                    }
                    return roleRepository.findNameById(user.getRoleId())
                            .switchIfEmpty(Mono.error(
                                    new BusinessException(BusinessError.ROLE_NOT_FOUND, "Rol inválido")))
                            .flatMap(roleName -> {
                                user.setRoleName(roleName);
                                return jwtSigner.sign(user);
                            });
                });
    }
}
