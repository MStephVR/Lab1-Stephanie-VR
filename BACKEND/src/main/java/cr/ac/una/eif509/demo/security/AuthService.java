package cr.ac.una.eif509.demo.security;

import cr.ac.una.eif509.demo.data.UsuarioRepository;
import cr.ac.una.eif509.demo.domain.UsuarioEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long expiracionSegundos;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${sisganado.jwt.expiration-seconds:3600}") long expiracionSegundos
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.expiracionSegundos = expiracionSegundos;
    }

    public LoginSalida autenticar(LoginEntrada entrada) {
        UsuarioEntity usuario = entrada == null ? null
                : usuarioRepository.findByUsername(entrada.username()).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())
                || usuario.getPasswordHash() == null
                || !passwordEncoder.matches(entrada.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas.");
        }

        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(expiracionSegundos);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("sisganado-api")
                .issuedAt(ahora)
                .expiresAt(expiracion)
                .subject(usuario.getUsername())
                .claim("roles", List.of(usuario.getRol()))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader.with(MacAlgorithm.HS256).build(), claims
        )).getTokenValue();
        return new LoginSalida(token, "Bearer", expiracion);
    }
}
