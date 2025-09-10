package co.com.bancolombia.api.config;


import co.com.bancolombia.api.dto.response.CustomResponse;
import co.com.bancolombia.api.dto.response.ErrorItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    private final ObjectMapper mapper = new ObjectMapper();

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter granted = new JwtGrantedAuthoritiesConverter();
        granted.setAuthoritiesClaimName("role");
        granted.setAuthorityPrefix("");

        JwtAuthenticationConverter blocking = new JwtAuthenticationConverter();
        blocking.setJwtGrantedAuthoritiesConverter(granted);

        return new ReactiveJwtAuthenticationConverterAdapter(blocking);
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthConverter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((we, e) ->
                                writeCustomError(
                                        we,
                                        HttpStatus.UNAUTHORIZED,
                                        "UNAUTHORIZED",
                                        "No autenticado. Envia un token Bearer valido en el header Authorization."
                                )
                        )
                        .accessDeniedHandler((we, e) ->
                                writeCustomError(
                                        we,
                                        HttpStatus.FORBIDDEN,
                                        "FORBIDDEN",
                                        "No tienes permiso para este recurso. Requiere un rol diferente (ADMIN / ADVISER)."
                                )
                        )
                )
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                        .pathMatchers("/oauth2/jwks").permitAll()
                        .pathMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/usuarios", "/api/v1/usuarios/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/v1/usuarios/getUserByDocument/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/usuarios/getUserByEmail/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/usuarios", "/api/v1/usuarios/**").hasAnyAuthority("ROLE_ADMIN","ROLE_ADVISER")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                )
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return authentication -> Mono.error(new RuntimeException("Use /api/v1/login"));
    }

    private Mono<Void> writeCustomError(ServerWebExchange exchange,
                                        HttpStatus status,
                                        String errorCode,
                                        String detail) {
        var req = exchange.getRequest();
        var res = exchange.getResponse();

        if (res.isCommitted()) return Mono.empty();

        res.setStatusCode(status);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (status == HttpStatus.UNAUTHORIZED) {
            res.getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }

        String traceId = Optional.ofNullable(req.getHeaders().getFirst("X-Trace-Id"))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString().substring(0, 13));

        var body = CustomResponse.<Object>fail(status.value(), "Business Exception")
                .toBuilder()
                .data(null)
                .errors(List.of(
                        ErrorItem.builder()
                                .field("business")
                                .code(errorCode)
                                .message(detail)
                                .build()
                ))
                .path(req.getPath().value())
                .traceId(traceId)
                .build();

        try {
            byte[] bytes = mapper.writeValueAsBytes(body);
            DataBuffer buffer = res.bufferFactory().wrap(bytes);
            return res.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            String fallback = """
          {"success":false,"code":%d,"message":"Business Exception","errors":[{"field":"business","code":"%s","message":"%s"}],"path":"%s","traceId":"%s"}
          """.formatted(status.value(), errorCode, detail, req.getPath().value(), traceId);
            DataBuffer buffer = res.bufferFactory().wrap(fallback.getBytes(StandardCharsets.UTF_8));
            return res.writeWith(Mono.just(buffer));
        }
    }
}
