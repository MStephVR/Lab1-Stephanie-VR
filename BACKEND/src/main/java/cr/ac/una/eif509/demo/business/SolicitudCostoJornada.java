package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SolicitudCostoJornada(
        @NotNull @Min(1) Integer cantidadAnimales,
        @NotNull @DecimalMin(value = "0.01") Double pesoPromedioKg,
        @NotNull @DecimalMin(value = "0.01") Double dosisMlPorKg,
        @NotNull @DecimalMin(value = "0.01") Double contenidoMlPorUnidad,
        @NotNull @DecimalMin(value = "0.00") BigDecimal precioProducto,
        @NotNull @DecimalMin(value = "0.00") BigDecimal manoObra,
        @NotNull @DecimalMin(value = "0.00") BigDecimal transporte,
        @NotNull @DecimalMin(value = "0.00") BigDecimal veterinario,
        @NotNull @DecimalMin(value = "0.00") BigDecimal otros
) {
}