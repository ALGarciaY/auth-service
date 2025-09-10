package co.com.bancolombia.model.user.gateways;

import co.com.bancolombia.model.user.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleRepository {
    Flux<Role> findAll();
    Mono<Role> create(Role role);
    Mono<Boolean> existsById(Integer id);
    Mono<String> findNameById(Integer roleId);
}
