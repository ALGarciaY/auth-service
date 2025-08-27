package co.com.bancolombia.api.dto.response;

import co.com.bancolombia.model.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private BigDecimal baseSalary;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .address(u.getAddress())
                .birthDate(u.getBirthDate())
                .baseSalary(u.getBaseSalary())
                .build();
    }
}
