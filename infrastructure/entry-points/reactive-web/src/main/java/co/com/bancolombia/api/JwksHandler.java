package co.com.bancolombia.api;


import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

@Component
public class JwksHandler {

    private final KeyPair keyPair;

    public JwksHandler(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public reactor.core.publisher.Mono<ServerResponse> jwks(ServerRequest request) {
        RSAKey rsa = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyID("auth-service-rsa")
                .build();

        JWKSet jwkSet = new JWKSet(rsa);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jwkSet.toJSONObject());
    }
}