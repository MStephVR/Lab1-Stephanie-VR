package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "planes_sanitarios")
public class PlanSanitarioEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nombre;
    private String descripcion;
    @Column(name = "edad_objetivo_meses")
    private Integer edadObjetivoMeses;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "plan_producto", joinColumns = @JoinColumn(name = "plan_id"), inverseJoinColumns = @JoinColumn(name = "producto_id"))
    private Set<ProductoVeterinarioEntity> productos = new HashSet<>();

    protected PlanSanitarioEntity() { }
    public PlanSanitarioEntity(String nombre, String descripcion, Integer edadObjetivoMeses) {
        this.nombre = nombre; this.descripcion = descripcion; this.edadObjetivoMeses = edadObjetivoMeses;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getEdadObjetivoMeses() { return edadObjetivoMeses; }
    public void setEdadObjetivoMeses(Integer value) { this.edadObjetivoMeses = value; }
    public Set<ProductoVeterinarioEntity> getProductos() { return productos; }
}
