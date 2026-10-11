package cr.ac.una.eif509.demo.business;

import java.math.BigDecimal;

public interface CostoProductoStrategy {
    int calcularUnidadesRequeridas(double dosisTotalMl, double contenidoMlPorUnidad);

    BigDecimal calcularCostoProducto(BigDecimal precioUnitario, int unidadesRequeridas);
}
