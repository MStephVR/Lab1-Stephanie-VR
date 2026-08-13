package cr.ac.una.eif509.demo.domain;

public record Proveedor(
        Long id,
        String nombre,
        String telefono,
        String correo
) {
}