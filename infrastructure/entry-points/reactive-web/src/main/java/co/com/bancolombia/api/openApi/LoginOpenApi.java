package co.com.bancolombia.api.openApi;

import co.com.bancolombia.api.dto.request.LoginRequest;
import co.com.bancolombia.model.user.AuthToken;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

@UtilityClass
public class LoginOpenApi {

    private final String TAG = "Autenticacion";
    private final String OK = "Success";
    private final String BAD_REQUEST = HttpStatus.BAD_REQUEST.getReasonPhrase();
    private final String UNAUTHORIZED = HttpStatus.UNAUTHORIZED.getReasonPhrase();
    private final String INTERNAL_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();

    public void login(Builder builder) {
        builder
                .operationId("login")
                .summary("Iniciar sesion")
                .description("Autentica al usuario por email y contraseña y devuelve un JWT")
                .tag(TAG)
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(LoginRequest.class))
        ))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.OK.value())).description(OK)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(AuthToken.class))
                        ))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.BAD_REQUEST.value())).description(BAD_REQUEST))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.UNAUTHORIZED.value())).description(UNAUTHORIZED))
                .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())).description(INTERNAL_ERROR));
    }
}
