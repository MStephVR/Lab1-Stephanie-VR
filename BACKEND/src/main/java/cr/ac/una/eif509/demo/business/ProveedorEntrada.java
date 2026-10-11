package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorEntrada(
        @NotBlank @Size(max = 160) String nombre,
        @Size(max = 40) String telefono,
        @Email @Size(max = 160) String correo
) { }
