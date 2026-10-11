package cr.ac.una.eif509.demo.business;

import java.time.LocalDate;

public record AplicacionJornadaSalida(
        Long animalId,
        Long productoId,
        LocalDate fechaAplicacion,
        Double dosisAplicada,
        LocalDate proximaFechaAplicacion,
        String observacion
) {
}
