package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.DtoValidator;
import co.com.bancolombia.api.dto.request.CreateUserRequest;
import co.com.bancolombia.api.dto.response.CustomResponse;
import co.com.bancolombia.api.dto.response.UserResponse;
import co.com.bancolombia.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {
    private final UserUseCase useCase;
    private final DtoValidator validator;

    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(CreateUserRequest.class)
                .doOnNext(body -> log.info("CreateUserRequest: {}", body))
                .flatMap(validator::validate)
                .map(CreateUserRequest::toDomain)
                .flatMap(useCase::createUser)
                .map(UserResponse::from)
                .flatMap(saved -> ServerResponse
                        .created(URI.create("/api/v1/user/" + saved.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(CustomResponse.created(saved)
                                .toBuilder()
                                .path(serverRequest.path())
                                .traceId(serverRequest.exchange().getRequest().getId())
                                .build()));
    }

    public Mono<ServerResponse> getUserById(ServerRequest serverRequest) {
        return useCase.getUserById(serverRequest.pathVariable("id"))
                .map(UserResponse::from)
                .doOnNext(user -> log.info("getUserById: {}", user))
                .flatMap( user -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(CustomResponse.ok(user)
                                .toBuilder()
                                .path(serverRequest.path())
                                .traceId(serverRequest.exchange().getRequest().getId())
                                .build()));

    }

    public Mono<ServerResponse> getAllUsers(ServerRequest serverRequest) {
        return useCase.getAllUsers()
                .map(UserResponse::from)
                .collectList()
                .flatMap(users -> {
                    var response = CustomResponse.ok(users)
                            .toBuilder()
                            .path(serverRequest.path())
                            .traceId(serverRequest.exchange().getRequest().getId())
                            .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }
}
