package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.springframework.data.jpa.domain.Specification;

public final class BusinessSpecifications {
    private BusinessSpecifications() { }

    public static Specification<AnimalEntity> animalesActivosDeRaza(String raza) {
        return animalesPorEstadoYRaza("Activo", raza);
    }

    public static Specification<AnimalEntity> animalesPorEstadoYRaza(String estado, String raza) {
        return animalesPorEstadoRazaYLote(estado, raza, null);
    }

    public static Specification<AnimalEntity> animalesPorEstadoRazaYLote(
            String estado, String raza, Long loteId) {
        return (root, query, builder) -> builder.and(
                estado == null || estado.isBlank()
                        ? builder.conjunction()
                        : builder.equal(builder.lower(root.get("estado")), estado.toLowerCase()),
                raza == null || raza.isBlank()
                        ? builder.conjunction()
                        : builder.equal(builder.lower(root.get("raza")), raza.toLowerCase()),
                loteId == null
                        ? builder.conjunction()
                        : builder.equal(root.get("lote").get("id"), loteId)
        );
    }

    public static Specification<ProductoVeterinarioEntity> productosDentroDelRangoDePrecio(
            Double minimo, Double maximo) {
        return (root, query, builder) -> builder.and(
                minimo == null ? builder.conjunction() : builder.ge(root.get("precioUnitario"), minimo),
                maximo == null ? builder.conjunction() : builder.le(root.get("precioUnitario"), maximo)
        );
    }
}