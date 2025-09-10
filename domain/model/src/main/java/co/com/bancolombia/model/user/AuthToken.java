package co.com.bancolombia.model.user;

import lombok.Builder;

@Builder
public record AuthToken(String token, long expiresInSeconds, String role, String email) {}
