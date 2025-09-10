package co.com.bancolombia.api.config;

import co.com.bancolombia.model.user.gateways.JwtSigner;
import co.com.bancolombia.model.user.gateways.PasswordHasher;
import co.com.bancolombia.model.user.gateways.RoleRepository;
import co.com.bancolombia.model.user.gateways.UserRepository;
import co.com.bancolombia.usecase.user.AuthUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UsesCasesConfig {

    @Bean
    public AuthUseCase authUseCase(UserRepository repository,
                                   RoleRepository roleRepository,
                                   PasswordHasher hasher,
                                   JwtSigner signer) {
        return new AuthUseCase(repository, roleRepository, hasher, signer);
    }
}
