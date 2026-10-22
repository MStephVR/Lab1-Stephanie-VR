package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.DatosCostoProductoInvalidosException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CostoPorMililitroStrategy implements CostoProductoStrategy {
    private final CostoPorUnidadStrategy costoPorUnidadStrategy;

    public CostoPorMililitroStrategy(CostoPorUnidadStrategy costoPorUnidadStrategy) {
        this.costoPorUnidadStrategy = costoPorUnidadStrategy;
    }

    @Override
    public int calcularUnidadesRequeridas(double dosisTotalMl, double contenidoMlPorUnidad) {
        return costoPorUnidadStrategy.calcularUnidadesRequeridas(dosisTotalMl, contenidoMlPorUnidad);
    }

    @Override
    public BigDecimal calcularCostoProducto(BigDecimal precioUnitario, int unidadesRequeridas) {
        return costoPorUnidadStrategy.calcularCostoProducto(precioUnitario, unidadesRequeridas);
    }

    @Override
    public BigDecimal calcularCostoProducto(
            BigDecimal precioUnitario, double dosisTotalMl, double contenidoMlPorUnidad
    ) {
        if (precioUnitario == null || precioUnitario.signum() < 0
                || !Double.isFinite(dosisTotalMl) || dosisTotalMl < 0
                || !Double.isFinite(contenidoMlPorUnidad) || contenidoMlPorUnidad <= 0) {
            throw new DatosCostoProductoInvalidosException(
                    "Los datos para calcular el costo proporcional por mililitro no son válidos.");
        }
        return precioUnitario
                .multiply(BigDecimal.valueOf(dosisTotalMl))
                .divide(BigDecimal.valueOf(contenidoMlPorUnidad), 2, RoundingMode.HALF_UP);
    }
}
