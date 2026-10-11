package cr.ac.una.eif509.demo.business;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventarioSalida(Long id, Long productoId, String loteFabricacion,
                               LocalDate fechaVencimiento, Integer cantidadDisponible,
                               BigDecimal costoUnitario) { }
