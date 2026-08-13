package cr.ac.una.eif509.demo.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Compra(
        Long id,
        Long proveedorId,
        LocalDate fechaCompra,
        BigDecimal total
) {
}