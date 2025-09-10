package co.com.bancolombia.api.config;

import co.com.bancolombia.model.user.AuthToken;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.JwtSigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import reactor.core.publisher.Mono;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;

@Configuration
public class JwtSignerConfig {

    private final String issuer;
    private final String audience;
    private final long expiration;

    public JwtSignerConfig(
            @Value("${security.jwt.issuer:auth-service}") String issuer,
            @Value("${security.jwt.audience:loan-service}") String audience,
            @Value("${security.jwt.expiration-sec:3600}") long expiration
    ) {
        this.issuer = issuer;
        this.audience = audience;
        this.expiration = expiration;
    }

    @Bean
    public KeyPair keyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return kpg.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el RSA KeyPair", e);
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair keyPair) {
        RSAKey rsa = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .keyID("auth-service-rsa")
                .build();
        var jwkSource = new ImmutableJWKSet<SecurityContext>(new JWKSet(rsa));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(KeyPair keyPair) {
        NimbusReactiveJwtDecoder decoder =
                NimbusReactiveJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic())
                        .build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> withAudience = token -> {
            List<String> aud = token.getAudience();
            boolean ok = aud != null && aud.contains(audience);
            return ok
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Missing or invalid audience", null));
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    public JwtSigner jwtSigner(JwtEncoder encoder) {
        return (User user) -> {
            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(expiration))
                    .subject(user.getEmail())
                    .audience(List.of(audience))
                    .claim("uid", user.getId())
                    .claim("role", "ROLE_" + user.getRoleName()) // Spring espera ROLE_*
                    .build();

            JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256).build();
            String token = encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();

            return Mono.just(AuthToken.builder()
                    .token(token)
                    .expiresInSeconds(expiration)
                    .role(user.getRoleName())
                    .email(user.getEmail())
                    .build());
        };
    }
}
