package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.ReglaNegocioException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CostoPorUnidadStrategy implements CostoProductoStrategy {

    @Override
    public int calcularUnidadesRequeridas(double dosisTotalMl, double contenidoMlPorUnidad) {
        if (!Double.isFinite(dosisTotalMl) || dosisTotalMl < 0
                || !Double.isFinite(contenidoMlPorUnidad) || contenidoMlPorUnidad <= 0) {
            throw new ReglaNegocioException("La dosis y el contenido por unidad deben ser válidos.");
        }
        double unidades = Math.ceil(dosisTotalMl / contenidoMlPorUnidad);
        if (!Double.isFinite(unidades) || unidades > Integer.MAX_VALUE) {
            throw new ReglaNegocioException("La cantidad de unidades requerida excede el rango permitido.");
        }
        return (int) unidades;
    }

    @Override
    public BigDecimal calcularCostoProducto(BigDecimal precioUnitario, int unidadesRequeridas) {
        if (precioUnitario == null || precioUnitario.signum() < 0 || unidadesRequeridas < 0) {
            throw new ReglaNegocioException("El precio y la cantidad de unidades deben ser válidos.");
        }
        return precioUnitario.multiply(BigDecimal.valueOf(unidadesRequeridas))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
