package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraEntrada(
        @NotNull @Positive Long proveedorId,
        @NotNull @PastOrPresent LocalDate fechaCompra,
        @NotNull @DecimalMin("0.00") BigDecimal total
) { }
