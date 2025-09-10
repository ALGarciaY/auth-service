package co.com.bancolombia.model.user.gateways;

import co.com.bancolombia.model.user.AuthToken;
import co.com.bancolombia.model.user.User;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface JwtSigner {
    Mono<AuthToken> sign(User user);
}
