package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProcesarCostoJornadaEntrada(
        @NotNull @Positive Long jornadaId,
        @NotNull @DecimalMin("0.00") BigDecimal manoObra,
        @NotNull @DecimalMin("0.00") BigDecimal transporte,
        @NotNull @DecimalMin("0.00") BigDecimal veterinario,
        @NotNull @DecimalMin("0.00") BigDecimal otros
) {
}
