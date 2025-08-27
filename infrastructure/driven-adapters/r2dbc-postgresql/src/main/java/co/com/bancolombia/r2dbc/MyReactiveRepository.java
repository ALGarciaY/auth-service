package co.com.bancolombia.r2dbc;

import co.com.bancolombia.r2dbc.data.UserData;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

// TODO: This file is just an example, you should delete or modify it
public interface MyReactiveRepository extends ReactiveCrudRepository<UserData, String>, ReactiveQueryByExampleExecutor<UserData> {

    Mono<Boolean> existsByEmail(String email);
}
