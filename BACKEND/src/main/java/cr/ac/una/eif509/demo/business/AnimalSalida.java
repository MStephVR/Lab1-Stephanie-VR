package cr.ac.una.eif509.demo.business;

import java.time.LocalDate;

public record AnimalSalida(
        Long id, String codigoArete, String raza, String sexo, Double pesoKg,
        LocalDate fechaNacimiento, String estado, Long loteId
) { }
