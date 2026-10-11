package cr.ac.una.eif509.demo.business;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroJornadaSalida(
        Long id,
        Long loteId,
        Long planId,
        LocalDate fecha,
        String descripcion,
        Integer cantidadAnimales,
        BigDecimal costoTotal,
        String mensaje
) {
}
