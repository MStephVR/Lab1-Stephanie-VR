package cr.ac.una.eif509.demo.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JornadaSanitaria(
        Long id,
        LocalDate fecha,
        String descripcion,
        Integer cantidadAnimales,
        BigDecimal costoTotal
) {
}