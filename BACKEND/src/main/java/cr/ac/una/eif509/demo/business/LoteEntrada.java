package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoteEntrada(
        @NotBlank @Size(max = 160) String nombre,
        @Size(max = 200) String ubicacion,
        @Size(max = 100) String proposito,
        boolean activo
) { }
