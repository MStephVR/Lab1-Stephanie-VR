package cr.ac.una.eif509.demo.domain;

public record ProductoVeterinario(
        Long id,
        String nombre,
        String tipo,
        String unidadMedida,
        Double dosisMinima,
        Double dosisMaxima,
        Double precioUnitario
) {
}