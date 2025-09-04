package co.com.bancolombia.model.user.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessError {
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS",
            "El correo ya se encuentra registrado.",
            409),

    USERS_NOT_FOUND("USERS_NOT_FOUND",
            "No hay usuarios registrados.",
            404),

    RECORDS_NOT_FOUND("RECORDS_NOT_FOUND",
            "No se encontraron registros.",
            404),

    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION",
            "Regla de negocio violada.",
            422);

    private final String code;
    private final String defaultMsg;
    private final Integer status;
}
