package cr.ac.una.eif509.demo.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AnimalEntrada(
        @NotBlank @Size(max = 80) String codigoArete,
        @NotBlank @Size(max = 100) String raza,
        @NotBlank @Size(max = 20) String sexo,
        @NotNull @Positive Double pesoKg,
        @NotNull @PastOrPresent LocalDate fechaNacimiento,
        @NotBlank String estado,
        @NotNull @Positive Long loteId
) { }
