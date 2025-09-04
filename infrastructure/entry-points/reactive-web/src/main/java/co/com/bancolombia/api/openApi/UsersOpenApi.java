package co.com.bancolombia.api.openApi;

import co.com.bancolombia.api.dto.request.CreateUserRequest;
import co.com.bancolombia.api.dto.response.CustomResponse;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

@UtilityClass
public class UsersOpenApi {

    private final String TAG = "Usuarios";
    private final String OK = "Success";
    private final String BAD_REQUEST = HttpStatus.BAD_REQUEST.getReasonPhrase();
    private final String NOT_FOUND = HttpStatus.NOT_FOUND.getReasonPhrase();
    private final String CONFLICT = HttpStatus.CONFLICT.getReasonPhrase();
    private final String INTERNAL_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();

    public Builder createUser(Builder builder) {
        return builder
                .operationId("createUser")
                .summary("Crea un nuevo usuario")
                .description("Registra un nuevo usuario en el sistema")
                .tag(TAG)
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CreateUserRequest.class))))
                // 200 o 201, según tu decisión de contrato. Aquí mantengo 200 como tu ejemplo previo.
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.OK.value())).description("Usuario creado")
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.BAD_REQUEST.value())).description(BAD_REQUEST)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.CONFLICT.value())).description(CONFLICT) // p.ej. email ya existe
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())).description(INTERNAL_ERROR)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))));
    }

    public Builder getAllUsers(Builder builder) {
        return builder
                .operationId("getAllUsers")
                .summary("Obtener todos los usuarios")
                .description("Devuelve la lista de usuarios")
                .tag(TAG)
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.OK.value())).description(OK)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.NOT_FOUND.value())).description(NOT_FOUND) // si decides emitir 404 cuando no hay registros
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())).description(INTERNAL_ERROR)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))));
    }

    public Builder getUserById(Builder builder) {
        return builder
                .operationId("getUserById")
                .summary("Obtener un usuario por ID")
                .description("Busca un usuario por su identificador")
                .tag(TAG)
                .parameter(parameterBuilder()
                        .name("id")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .description("ID del usuario")
                        .schema(schemaBuilder().implementation(String.class))
                        .example("42"))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.OK.value())).description(OK)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.NOT_FOUND.value())).description(NOT_FOUND)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))))
                .response(responseBuilder().responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())).description(INTERNAL_ERROR)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CustomResponse.class))));
    }
}
