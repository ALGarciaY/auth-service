package co.com.bancolombia.api.config;

import co.com.bancolombia.api.dto.response.CustomResponse;
import co.com.bancolombia.api.dto.response.ErrorItem;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Component
public class FunctionalErrorFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return next.handle(request)

                .onErrorResume(ConstraintViolationException.class, e -> badRequestFromConstraintViolations(e, request))
                .onErrorResume(ServerWebInputException.class, e -> badRequestFromServerWebInput(e, request))
                .onErrorResume(WebExchangeBindException.class, e -> badRequestFromBindException(e, request))
                .onErrorResume(DecodingException.class, e -> badRequestFromDecoding(e, request))
                .onErrorResume(IllegalStateException.class, e -> conflict(e.getMessage(), request))
                .onErrorResume(Throwable.class, e -> {
                    log.error("Unexpected error", e);
                    var body = CustomResponse.<Void>fail(INTERNAL_SERVER_ERROR.value(), "Unexpected error")
                            .toBuilder()
                            .path(request.path())
                            .traceId(request.exchange().getRequest().getId())
                            .build();
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(body);
                });
    }

    private Mono<ServerResponse> conflict(String message, ServerRequest req) {
        var body = CustomResponse.<Void>fail(CONFLICT.value(), message != null ? message : "Conflict")
                .toBuilder()
                .path(req.path())
                .traceId(req.exchange().getRequest().getId())
                .build();
        return ServerResponse.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    private Mono<ServerResponse> badRequestFromServerWebInput(ServerWebInputException e, ServerRequest req) {
        var errorItem = extractJsonError(e)
                .or(() -> extractJsonError(e))   // <--- Fallback por regex
                .orElseGet(() -> ErrorItem.of("body", "InvalidFormat",
                        e.getReason() != null ? e.getReason() : "Invalid request"));

        var body = CustomResponse.<Void>validation(List.of(errorItem))
                .toBuilder()
                .path(req.path())
                .traceId(req.exchange().getRequest().getId())
                .build();

        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }
    private Mono<ServerResponse> badRequestFromDecoding(DecodingException e, ServerRequest req) {
        var errorItem = extractJsonError(e)
                .orElse(ErrorItem.of("body", "InvalidFormat", "Cuerpo JSON inválido"));

        var body = CustomResponse.<Void>validation(List.of(errorItem))
                .toBuilder()
                .path(req.path())
                .traceId(req.exchange().getRequest().getId())
                .build();

        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }

    private Optional<ErrorItem> extractJsonError(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof DecodingException de && de.getCause() != null) {
                cur = de.getCause();
                continue;
            }
            if (cur instanceof JsonParseException jpe) {
                return Optional.of(ErrorItem.of("body", "MalformedJson",
                        "JSON mal formado: " + safeMsg(jpe.getOriginalMessage())));
            }
            if (cur instanceof UnrecognizedPropertyException upe) {
                String field = upe.getPropertyName();
                return Optional.of(ErrorItem.of(field, "UnknownField",
                        "El campo '%s' no existe.".formatted(field)));
            }
            if (cur instanceof InvalidFormatException ife) {
                String field = lastPath(ife);
                Class<?> target = ife.getTargetType();
                return Optional.of(ErrorItem.of(field, "InvalidType", buildTypeMessage(field, target)));
            }
            if (cur instanceof MismatchedInputException mie) {
                String field = lastPath(mie);
                Class<?> target = mie.getTargetType();
                return Optional.of(ErrorItem.of(field, "InvalidType", buildTypeMessage(field, target)));
            }
            if (cur instanceof JsonMappingException jme) {
                String field = lastPath(jme);
                return Optional.of(ErrorItem.of(
                        field,
                        "InvalidFormat",
                        safeMsg(jme.getOriginalMessage())));
            }
            cur = cur.getCause();
        }
        return Optional.empty();
    }

    private static String lastPath(JsonMappingException jme) {
        var path = jme.getPath();
        if (path == null || path.isEmpty()) return "body";
        var last = path.get(path.size() - 1);
        return last.getFieldName() != null ? last.getFieldName() : "body";
    }

    private static String buildTypeMessage(String field, Class<?> target) {
        if (target != null && isNumeric(target)) {
            return "El campo '%s' debe ser numérico.".formatted(field);
        }
        String expected = target != null ? target.getSimpleName() : "tipo correcto";
        return "El campo '%s' tiene formato inválido. Se esperaba %s.".formatted(field, expected);
    }

    private static boolean isNumeric(Class<?> cls) {
        return Number.class.isAssignableFrom(cls)
                || cls == int.class || cls == long.class || cls == double.class || cls == float.class
                || "BigDecimal".equalsIgnoreCase(cls.getSimpleName())
                || "BigInteger".equalsIgnoreCase(cls.getSimpleName());
    }

    private static String safeMsg(String msg) {
        return msg == null ? "Error de formato" : msg;
    }

    private Mono<ServerResponse> badRequestFromBindException(WebExchangeBindException e, ServerRequest req) {
        var errors = e.getFieldErrors().stream()
                .map(fe -> ErrorItem.of(fe.getField(), fe.getCode(), fe.getDefaultMessage()))
                .toList();
        var body = CustomResponse.<Void>validation(errors)
                .toBuilder()
                .path(req.path())
                .traceId(req.exchange().getRequest().getId())
                .build();
        return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(body);
    }

    private Mono<ServerResponse> badRequestFromConstraintViolations(ConstraintViolationException e, ServerRequest req) {
        var errors = e.getConstraintViolations().stream()
                .map(v -> ErrorItem.of(
                        v.getPropertyPath().toString(),
                        v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                        v.getMessage()))
                .toList();
        var body = CustomResponse.<Void>validation(errors)
                .toBuilder()
                .path(req.path())
                .traceId(req.exchange().getRequest().getId())
                .build();
        return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(body);
    }
}
