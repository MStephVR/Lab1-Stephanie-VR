package cr.ac.una.eif509.demo.domain;

import java.time.LocalDate;

public record Animal(
        Long id,
        String codigoArete,
        String raza,
        String sexo,
        Double pesoKg,
        LocalDate fechaNacimiento,
        String estado,
        Long loteId
) {
}