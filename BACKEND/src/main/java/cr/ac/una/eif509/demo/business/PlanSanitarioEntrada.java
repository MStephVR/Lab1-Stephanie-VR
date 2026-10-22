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
        @PositiveOrZero Integer intervaloMinimoDias,
        @NotEmpty List<@NotNull @jakarta.validation.constraints.Positive Long> productosIds
) {
    public PlanSanitarioEntrada(String nombre, String descripcion, Integer edadObjetivoMeses,
                                List<Long> productosIds) {
        this(nombre, descripcion, edadObjetivoMeses, null, productosIds);
    }
}
