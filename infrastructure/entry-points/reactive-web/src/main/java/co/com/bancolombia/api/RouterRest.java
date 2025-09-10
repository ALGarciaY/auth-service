package co.com.bancolombia.api;

import co.com.bancolombia.api.errors.FunctionalErrorFilter;
import co.com.bancolombia.api.openApi.LoginOpenApi;
import co.com.bancolombia.api.path.LoginPath;
import co.com.bancolombia.api.path.UsersPath;
import co.com.bancolombia.api.openApi.UsersOpenApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final FunctionalErrorFilter errorFilter;
    private final UsersPath usersPath;
    private final LoginPath loginPath;
    private final Handler handler;
    private final LoginHandler loginHandler;
    private final JwksHandler jwksHandler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route()
                .POST(usersPath.createUser(),  handler::createUser,   UsersOpenApi::createUser)
                .POST(loginPath.login(), loginHandler::login, LoginOpenApi::login)
                .GET (usersPath.getAllUsers(), handler::getAllUsers,  UsersOpenApi::getAllUsers)
                .GET (usersPath.getUserById(), handler::getUserById,  UsersOpenApi::getUserById)
                .GET (usersPath.getUserByEmail(), handler::getUserByEmail, UsersOpenApi::getUserByEmail)
                .GET (usersPath.getUserByDocument(), handler::getUserByDocument, UsersOpenApi::getUserByDocument)
                .GET("/oauth2/jwks", jwksHandler::jwks, b -> b
                        .operationId("jwks")
                        .summary("JWKS publico")
                        .description("Devuelve el JSON Web Key Set con la(s) clave(s) publica(s) para verificar JWT")
                        .tag("Auth")
                )
                .build()
                .filter(errorFilter);
    }
}
