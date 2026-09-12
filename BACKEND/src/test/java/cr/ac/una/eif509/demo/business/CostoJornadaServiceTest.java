package cr.ac.una.eif509.demo.business;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CostoJornadaServiceTest {

    @Test
    void calcularCostoJornada_redondeaUnidadesHaciaArriba() {
        CostoJornadaService service = new CostoJornadaService();

        Map<String, Object> resultado = service.calcularCostoJornada(
                2,
                150.0,
                0.5,
                100.0,
                new BigDecimal("2500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("150.00"),
                new BigDecimal("200.00"),
                new BigDecimal("100.00")
        );

        assertEquals(2.0, (Double) resultado.get("unidadesRequeridas"));
        assertEquals(new BigDecimal("5000.00"), resultado.get("costoProducto"));
    }
}
