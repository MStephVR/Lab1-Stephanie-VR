package cr.ac.una.eif509.demo.business;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraSalida(Long id, Long proveedorId, LocalDate fechaCompra, BigDecimal total) { }
