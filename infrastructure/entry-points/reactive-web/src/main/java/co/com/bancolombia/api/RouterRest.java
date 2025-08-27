package co.com.bancolombia.api;

import co.com.bancolombia.api.config.FunctionalErrorFilter;
import co.com.bancolombia.api.dto.request.CreateUserRequest;
import co.com.bancolombia.api.dto.response.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
@Tag(name = "Users", description = "API de gestión de usuarios")
public class RouterRest {

    private final FunctionalErrorFilter errorFilter;
    private static final String BASE_URL = "/api/v1/usuarios/";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/usuarios/getAllUsers",
                    beanClass = Handler.class,
                    beanMethod = "getAllUsers",
                    operation = @Operation(
                            operationId = "getAllUsers",
                            summary = "Obtener todos los usuarios",
                            tags = {"Usuarios"},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Lista de usuarios",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = CustomResponse.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Error en la petición",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = CustomResponse.class)
                                            )
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuarios/createUser",
                    beanClass = Handler.class,
                    beanMethod = "createUser",
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Crea un nuevo usuario",
                            description = "Registra un nuevo usuario en el sistema",
                            tags = {"Usuarios"},
                            requestBody = @RequestBody(
                                    description = "Datos del usuario",
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = CreateUserRequest.class)
                                    ),
                                    required = true
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario creado exitosamente",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = CustomResponse.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Error en la petición",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = CustomResponse.class)
                                            )
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuarios/getUserById/{id}",
                    beanClass = Handler.class,
                    beanMethod = "getUserById",
                    operation = @Operation(
                            operationId = "getUserById",
                            summary = "Obtener un usuario por ID",
                            tags = {"Usuarios"},
                            parameters = {
                                    @Parameter(name = "id", in = ParameterIn.PATH, required = true)
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = CustomResponse.class)
                                    )),
                                    @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(implementation = CustomResponse.class)
                                    ))
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(BASE_URL + "createUser"), handler::createUser)
                .andRoute(GET(BASE_URL + "getUserById/{id}"), handler::getUserById)
                .andRoute(GET(BASE_URL + "getAllUsers"), handler::getAllUsers)
                .filter(errorFilter);
    }
}
