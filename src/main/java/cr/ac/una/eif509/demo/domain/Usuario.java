package cr.ac.una.eif509.demo.domain;

public record Usuario(
        Long id,
        String nombreCompleto,
        String rol,
        Boolean activo
) {
}