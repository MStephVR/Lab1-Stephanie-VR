package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PlanificarJornadaEntrada(
        @NotNull @Positive Long loteId,
        @NotNull @Positive Long planId,
        @NotNull @FutureOrPresent LocalDate fecha,
        @NotBlank @Size(max = 200) String descripcion
) {
}
