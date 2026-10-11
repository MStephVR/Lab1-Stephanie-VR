package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventarioEntrada(
        @NotNull @jakarta.validation.constraints.Positive Long productoId,
        @NotBlank @Size(max = 100) String loteFabricacion,
        @NotNull @FutureOrPresent LocalDate fechaVencimiento,
        @NotNull @PositiveOrZero Integer cantidadDisponible,
        @NotNull @DecimalMin("0.00") BigDecimal costoUnitario
) { }
