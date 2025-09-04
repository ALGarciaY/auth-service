package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.errors.BusinessError;
import co.com.bancolombia.model.user.errors.BusinessException;
import co.com.bancolombia.model.user.gateways.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public record UserUseCase(UserRepository repository) {

    public Mono<User> createUser(User user) {
        return repository.existsByEmail(user.getEmail())
                .flatMap(exists -> exists
                        ? Mono.error(new BusinessException(
                        BusinessError.EMAIL_ALREADY_EXISTS,
                        "El correo %s ya se encuentra registrado."
                                .formatted(user.getEmail())))
                        : repository.save(user));
    }

    public Mono<User> getUserById(String id) {
        return repository.findById(id)
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
