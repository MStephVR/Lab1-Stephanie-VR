package cr.ac.una.eif509.demo.business;

import java.math.BigDecimal;

public record ProcesamientoCostoSalida(
        Long jornadaId,
        int cantidadAnimales,
        double dosisTotalMl,
        int unidadesConsumidas,
        BigDecimal costoProducto,
        BigDecimal costosAdicionales,
        BigDecimal costoTotal,
        BigDecimal costoPromedioPorAnimal
) {
}
