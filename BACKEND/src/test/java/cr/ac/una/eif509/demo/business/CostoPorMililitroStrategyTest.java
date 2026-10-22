package cr.ac.una.eif509.demo.business;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CostoPorMililitroStrategyTest {
    private final CostoPorMililitroStrategy strategy =
            new CostoPorMililitroStrategy(new CostoPorUnidadStrategy());

    @Test
    void calculaCostoProporcionalAlVolumenSoloParaPresupuestos() {
        BigDecimal costo = strategy.calcularCostoProducto(new BigDecimal("2500.00"), 150.0, 100.0);

        assertEquals(new BigDecimal("3750.00"), costo);
        assertEquals(2, strategy.calcularUnidadesRequeridas(150.0, 100.0));
    }
}
