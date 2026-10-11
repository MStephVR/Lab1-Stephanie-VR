package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlanSanitarioEntrada(
        @NotBlank @Size(max = 160) String nombre,
        @Size(max = 500) String descripcion,
        @PositiveOrZero Integer edadObjetivoMeses,
        @NotEmpty List<@NotNull @jakarta.validation.constraints.Positive Long> productosIds
) { }
