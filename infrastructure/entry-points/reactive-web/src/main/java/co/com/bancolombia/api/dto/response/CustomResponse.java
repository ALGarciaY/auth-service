package co.com.bancolombia.api.dto.response;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CustomResponse<T> {

    @Builder.Default
    private boolean success = true;

    private int code;
    private String message;
    private T data;

    private List<ErrorItem> errors;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private String path;
    private String traceId;

    public static <T> CustomResponse<T> ok(T data) {
        return CustomResponse.<T>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("OK")
                .data(data)
                .build();
    }

    public static <T> CustomResponse<T> created(T data) {
        return CustomResponse.<T>builder()
                .success(true)
                .code(HttpStatus.CREATED.value())
                .message("CREATED")
                .data(data)
                .build();
    }

    public static <T> CustomResponse<T> fail(int httpCode, String message) {
        return CustomResponse.<T>builder()
                .success(false)
                .code(httpCode)
                .message(message)
                .build();
    }

    public static <T> CustomResponse<T> validation(List<ErrorItem> errors) {
        return CustomResponse.<T>builder()
                .success(false)
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation error")
                .errors(errors)
                .build();
    }
}
