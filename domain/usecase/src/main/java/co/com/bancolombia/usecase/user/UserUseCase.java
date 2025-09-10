package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.errors.BusinessError;
import co.com.bancolombia.model.user.errors.BusinessException;
import co.com.bancolombia.model.user.gateways.PasswordHasher;
import co.com.bancolombia.model.user.gateways.RoleRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public record UserUseCase(UserRepository repository, RoleRepository roleRepository, PasswordHasher passwordHasher) {

    public Mono<User> createUser(User user) {
        return roleRepository.existsById(user.getRoleId())
                .flatMap(exists -> exists
                        ? Mono.just(user)
                        : Mono.error(new BusinessException(
                        BusinessError.ROLE_NOT_FOUND,
                        "El rol %s no existe".formatted(user.getRoleId()))))
                .flatMap(u -> repository.existsByEmail(u.getEmail())
                        .flatMap(dupe -> dupe
                                ? Mono.error(new BusinessException(
                                BusinessError.EMAIL_ALREADY_EXISTS,
                                "El correo %s ya se encuentra registrado.".formatted(u.getEmail())))
                                : Mono.just(u)))
                .flatMap(u ->
                        Mono.fromCallable(() -> {
                            String raw = (u.getPassword() != null && !u.getPassword().isBlank())
                                    ? u.getPassword().trim()
                                    : (u.getDocument() == null ? "" : u.getDocument().trim());
                            boolean looksBcrypt = raw.startsWith("$2a$") || raw.startsWith("$2b$") || raw.startsWith("$2y$");
                            u.setPassword(looksBcrypt ? raw : passwordHasher.encode(raw));
                            return u;
                        }).subscribeOn(Schedulers.parallel())
                )
                .flatMap(repository::save);
    }

    public Mono<User> getUserById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(
                        BusinessError.RECORDS_NOT_FOUND,
                        "Usuario no encontrado")));
    }

    public Mono<User> getUserByDocument(String document) {
        return repository.findByDocument(document)
                .switchIfEmpty(Mono.error(new BusinessException(
                        BusinessError.RECORDS_NOT_FOUND,
                        "Usuario no encontrado")));
    }

    public Mono<User> getUserByEmail(String email) {
        return repository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException(
                        BusinessError.RECORDS_NOT_FOUND,
                        "Usuario no encontrado")));
    }

    public Flux<User> getAllUsers() {
        return repository.findAll()
                .switchIfEmpty(Flux.error(new BusinessException(
                        BusinessError.USERS_NOT_FOUND,
                        "No hay usuarios registrados")));
    }
}
