package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BitacoraEntrada(
        @NotBlank @Size(max = 120) String entidad,
        @NotBlank @Size(max = 80) String accion,
        @NotBlank @Size(max = 160) String usuario,
        @NotBlank @Size(max = 2000) String detalle
) { }
