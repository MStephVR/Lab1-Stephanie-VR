package cr.ac.una.eif509.demo.business;

import java.time.LocalDate;
import java.util.List;

public record EjecucionJornadaSalida(
        Long jornadaId,
        String estado,
        int animalesTratados,
        List<AplicacionJornadaSalida> aplicaciones,
        LocalDate fechaEjecucion
) {
    public EjecucionJornadaSalida {
        aplicaciones = List.copyOf(aplicaciones);
    }
}
