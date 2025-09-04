package co.com.bancolombia.api.config.dummy;

import co.com.bancolombia.api.errors.FunctionalErrorFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class DummyRouter {

    private final FunctionalErrorFilter errorFilter;

    public DummyRouter(FunctionalErrorFilter errorFilter) {
        this.errorFilter = errorFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> dummyRoutes(DummyHandler handler) {
        return RouterFunctions
                .route(RequestPredicates.GET("/test/ok"), handler::ok)
                .andRoute(RequestPredicates.GET("/test/error"), handler::throwError)
                .filter(errorFilter);
    }
}
