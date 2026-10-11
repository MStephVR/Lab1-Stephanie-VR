package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.security.AuthService;
import cr.ac.una.eif509.demo.security.LoginEntrada;
import cr.ac.una.eif509.demo.security.LoginSalida;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginSalida> login(@Valid @RequestBody LoginEntrada entrada) {
        return ResponseEntity.ok(authService.autenticar(entrada));
    }
}
