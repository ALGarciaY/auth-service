package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.user.Role;
import co.com.bancolombia.model.user.gateways.RoleRepository;
import co.com.bancolombia.r2dbc.entity.RoleEntity;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class MyReactiveRolRepositoryAdapter extends ReactiveAdapterOperations<
        Role,
        RoleEntity,
        Integer,
        MyReactiveRolRepository
        > implements RoleRepository {
    private final MyReactiveRolRepository repository;
    private final ObjectMapper mapper;

    public MyReactiveRolRepositoryAdapter(MyReactiveRolRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Role.class));
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Role> create(Role role) {
        return null;
    }

    @Override
    public Mono<Boolean> existsById(Integer id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<String> findNameById(Integer roleId) {
        return repository.findById(roleId)
                .map(RoleEntity::getName);
    }
}
