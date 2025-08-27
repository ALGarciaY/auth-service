package co.com.bancolombia.usecase.user;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public record UserUseCase(UserRepository repository) {

    public Mono<User> createUser(User user) {
        return repository.existsByEmail(user.getEmail())
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalStateException("correo_electronico ya registrado"))
                        : repository.save(user));
    }

    public Mono<User> getUserById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalStateException("Usuario no encontrado")));
    }

    public Flux<User> getAllUsers() {
        return repository.findAll();
    }
}
