package co.com.bancolombia.api.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}