package co.com.bancolombia.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
public class UserEntity {
    @Id
    @Column("id")
    private String id;
    private String firstName;
    private String lastName;
    private String document;
    private String email;
    private BigDecimal baseSalary;
    private LocalDate birthDate;
    private String address;
    private String phone;
    private Integer roleId;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
