package co.com.bancolombia.api.path;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class UsersPath {
    private static final String BASE = "/api/v1/usuarios";

    public String createUser() { return BASE + "/createUser"; }
    public String getAllUsers() { return BASE + "/getAllUsers"; }
    public String getUserById() { return BASE + "/getUserById/{id}"; }
    public String getUserByEmail() { return BASE + "/getUserByEmail/{email}"; }
    public String getUserByDocument() { return BASE + "/getUserByDocument/{document}"; }
}
