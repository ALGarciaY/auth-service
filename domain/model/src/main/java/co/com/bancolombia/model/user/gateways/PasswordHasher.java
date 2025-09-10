package co.com.bancolombia.model.user.gateways;

public interface PasswordHasher {
    String encode(String raw);
    boolean matches(String raw, String hash);
}
