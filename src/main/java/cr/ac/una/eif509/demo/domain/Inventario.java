package cr.ac.una.eif509.demo.domain;

import java.time.LocalDate;

public record Inventario(
        Long id,
        Long productoId,
        String loteFabricacion,
        LocalDate fechaVencimiento,
        Integer cantidadDisponible,
        Double costoUnitario
) {
}