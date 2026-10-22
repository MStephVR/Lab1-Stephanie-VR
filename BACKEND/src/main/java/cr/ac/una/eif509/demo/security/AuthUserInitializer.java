package cr.ac.una.eif509.demo.security;

import cr.ac.una.eif509.demo.data.UsuarioRepository;
import cr.ac.una.eif509.demo.domain.UsuarioEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "docker", "dev", "prod", "test"})
public class AuthUserInitializer implements ApplicationRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String veterinarianPassword;

    public AuthUserInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${sisganado.auth.admin-password:AdminLocal2026!}") String adminPassword,
            @Value("${sisganado.auth.veterinarian-password:VetLocal2026!}") String veterinarianPassword
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.veterinarianPassword = veterinarianPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        crearUsuarioSiNoExiste("admin", "Administrador", "ADMIN", adminPassword);
        crearUsuarioSiNoExiste("veterinario", "Veterinario de prueba", "VETERINARIO", veterinarianPassword);
    }

    private void crearUsuarioSiNoExiste(String username, String nombre, String rol, String password) {
        if (password == null || password.length() < 12) {
            throw new IllegalStateException("Las contraseñas iniciales de autenticación deben tener 12 caracteres.");
        }
        if (usuarioRepository.findByUsername(username).isEmpty()) {
            UsuarioEntity usuario = new UsuarioEntity(nombre, rol, true);
            usuario.setUsername(username);
            usuario.setPasswordHash(passwordEncoder.encode(password));
            usuarioRepository.save(usuario);
        }
    }
}
