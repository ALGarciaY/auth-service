package co.com.bancolombia.api.path;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class LoginPath {
    private static final String BASE = "/api/v1";

    public String login() { return BASE + "/login"; }
}

