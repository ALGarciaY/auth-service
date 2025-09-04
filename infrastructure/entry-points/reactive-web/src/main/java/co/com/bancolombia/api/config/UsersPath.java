package co.com.bancolombia.api.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class UsersPath {
    private static final String BASE = "/api/v1/usuarios";

    public String createUser() { return BASE + "/createUser"; }
    public String getAllUsers() { return BASE + "/getAllUsers"; }
    public String getUserById() { return BASE + "/getUserById/{id}"; }
}
