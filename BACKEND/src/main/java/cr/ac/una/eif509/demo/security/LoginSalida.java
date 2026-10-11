package cr.ac.una.eif509.demo.security;

import java.time.Instant;

public record LoginSalida(String accessToken, String tokenType, Instant expiresAt) {
}
