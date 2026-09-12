package cr.ac.una.eif509.demo.domain;

import java.time.LocalDate;

public record AplicacionSanitaria(
        Long id,
        Long animalId,
        Long productoId,
        LocalDate fechaAplicacion,
        Double dosisAplicada,
        LocalDate proximaFechaAplicacion,
        String observacion
) {
}