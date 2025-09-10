/*package co.com.bancolombia.api;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class LoginRouterRest {
    private static final String BASE = "/api/v1";

    @Bean(name = "loginRoutes")
    public RouterFunction<ServerResponse> routerFunction(LoginHandler handler) {
        return route(POST(BASE + "/login"), handler::login);
    }
}*/
