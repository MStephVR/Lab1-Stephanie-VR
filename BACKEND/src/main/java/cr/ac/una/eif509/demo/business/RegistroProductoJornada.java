package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistroProductoJornada(
        @NotNull @Positive Long productoId,
        @NotNull @Positive Double dosisMlPorKg,
        @Size(max = 500) String observacion
) {
}
