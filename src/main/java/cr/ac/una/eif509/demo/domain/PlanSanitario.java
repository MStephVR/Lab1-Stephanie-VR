package cr.ac.una.eif509.demo.domain;

public record PlanSanitario(
        Long id,
        String nombre,
        String descripcion,
        Integer edadObjetivoMeses
) {
}