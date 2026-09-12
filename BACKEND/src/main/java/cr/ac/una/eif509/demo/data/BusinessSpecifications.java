package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.springframework.data.jpa.domain.Specification;

public final class BusinessSpecifications {
    private BusinessSpecifications() { }

    public static Specification<AnimalEntity> animalesActivosDeRaza(String raza) {
        return (root, query, builder) -> builder.and(
                builder.equal(root.get("estado"), "Activo"),
                raza == null || raza.isBlank()
                        ? builder.conjunction()
                        : builder.equal(builder.lower(root.get("raza")), raza.toLowerCase())
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