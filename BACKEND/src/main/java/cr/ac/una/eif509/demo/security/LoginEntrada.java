package cr.ac.una.eif509.demo.security;

import jakarta.validation.constraints.NotBlank;

public record LoginEntrada(@NotBlank String username, @NotBlank String password) {
}
