package cr.ac.una.eif509.demo.business;

import java.math.BigDecimal;

public record CostoJornada(
        int cantidadAnimales,
        double pesoPromedioKg,
        double dosisMlPorKg,
        double dosisPorAnimal,
        double dosisTotal,
        double unidadesRequeridas,
        BigDecimal costoProducto,
        BigDecimal manoObra,
        BigDecimal transporte,
        BigDecimal veterinario,
        BigDecimal otros,
        BigDecimal costoTotal,
        BigDecimal costoPromedioPorAnimal
) {
}