package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductoEntrada(
        @NotBlank @Size(max = 160) String nombre,
        @NotBlank @Size(max = 80) String tipo,
        @NotBlank @Size(max = 30) String unidadMedida,
        @DecimalMin("0.0") Double dosisMinima,
        @DecimalMin("0.0") Double dosisMaxima,
        @NotNull @DecimalMin("0.00") BigDecimal precioUnitario,
        @NotNull @Positive Double contenidoMlPorUnidad
) { }
