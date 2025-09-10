package co.com.bancolombia.api.config;

import co.com.bancolombia.model.user.gateways.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityCryptoConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public PasswordHasher passwordHasher(BCryptPasswordEncoder encoder) {
        return new PasswordHasher() {
            @Override
            public String encode(String raw) { return encoder.encode(raw);
            }

            @Override public boolean matches(String raw, String hashed) {
                return encoder.matches(raw, hashed); }
        };
    }
}
