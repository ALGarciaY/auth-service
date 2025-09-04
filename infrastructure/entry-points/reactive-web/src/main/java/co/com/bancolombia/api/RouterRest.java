package co.com.bancolombia.api;

import co.com.bancolombia.api.errors.FunctionalErrorFilter;
import co.com.bancolombia.api.config.UsersPath;
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
    private final Handler handler; // ajusta el tipo si tu handler tiene otro nombre

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route()
                .POST(usersPath.createUser(),  handler::createUser,   UsersOpenApi::createUser)
                .GET (usersPath.getAllUsers(), handler::getAllUsers,  UsersOpenApi::getAllUsers)
                .GET (usersPath.getUserById(), handler::getUserById,  UsersOpenApi::getUserById)
                .build()
                .filter(errorFilter);
    }
}
