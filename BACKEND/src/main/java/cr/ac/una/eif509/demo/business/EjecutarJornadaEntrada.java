package cr.ac.una.eif509.demo.business;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record EjecutarJornadaEntrada(
        @NotEmpty List<@NotNull @Positive Long> animales,
        @NotEmpty @Valid List<RegistroProductoJornada> productos
) {
}
