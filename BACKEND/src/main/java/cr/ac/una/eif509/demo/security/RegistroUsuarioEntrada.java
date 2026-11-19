package cr.ac.una.eif509.demo.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioEntrada(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,80}") String username,
        @NotBlank @Size(min = 2, max = 120) String nombreCompleto,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotNull TipoCuenta tipoCuenta
) {
    public enum TipoCuenta {
        VETERINARIO,
        USUARIO
    }
}
