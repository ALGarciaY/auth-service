package co.com.bancolombia.api.dto.request;

import co.com.bancolombia.model.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "firstName es obligatorio")
    private String firstName;

    @NotBlank(message = "lastName es obligatorio")
    private String lastName;

    @NotBlank(message = "document es obligatorio")
    @Size(min = 5, max = 15, message = "document debe tener entre 5 y 15 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "document solo puede contener caracteres alfanuméricos")
    private String document;

    @Email(message = "email invalido")
    @NotBlank(message = "email es obligatorio")
    private String email;

    @Pattern(regexp = "^[+\\d][\\d\\s-]{6,20}$", message = "phone inválido")
    @NotBlank(message = "phone es obligatorio")
    private String phone;

    @NotBlank(message = "address es obligatorio")
    private String address;

    @Past(message = "birthDate debe ser en el pasado")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message = "baseSalary es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "baseSalary debe ser > 0")
    @DecimalMax(value = "15000000.0", message = "baseSalary no puede ser mayor a 15,000,000")
    private BigDecimal baseSalary;

    @NotNull(message = "roleId es obligatorio")
    private Integer roleId;

    public User toDomain() {
        return User.builder()
                .id(null)
                .firstName(firstName)
                .lastName(lastName)
                .document(document)
                .email(email)
                .phone(phone)
                .address(address)
                .birthDate(birthDate)
                .baseSalary(baseSalary)
                .roleId(roleId)
                .build();
    }
}
