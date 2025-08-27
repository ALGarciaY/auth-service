package co.com.bancolombia.api.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorItem {
    private String field;
    private String code;
    private String message;

    public static ErrorItem of(String field, String code, String message) {
        return ErrorItem.builder().field(field).code(code).message(message).build();
    }
}
