package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "productos_veterinarios")
public class ProductoVeterinarioEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nombre;
    @Column(nullable = false)
    private String tipo;
    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida;
    @Column(name = "dosis_minima")
    private Double dosisMinima;
    @Column(name = "dosis_maxima")
    private Double dosisMaxima;
    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    private List<InventarioEntity> inventarios = new ArrayList<>();
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    private List<AplicacionSanitariaEntity> aplicaciones = new ArrayList<>();
    @ManyToMany(mappedBy = "productos", fetch = FetchType.LAZY)
    private Set<PlanSanitarioEntity> planes = new HashSet<>();

    protected ProductoVeterinarioEntity() { }
    public ProductoVeterinarioEntity(String nombre, String tipo, String unidadMedida, Double dosisMinima, Double dosisMaxima, BigDecimal precioUnitario) {
        this.nombre = nombre; this.tipo = tipo; this.unidadMedida = unidadMedida; this.dosisMinima = dosisMinima; this.dosisMaxima = dosisMaxima; this.precioUnitario = precioUnitario;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String value) { this.unidadMedida = value; }
    public Double getDosisMinima() { return dosisMinima; }
    public void setDosisMinima(Double value) { this.dosisMinima = value; }
    public Double getDosisMaxima() { return dosisMaxima; }
    public void setDosisMaxima(Double value) { this.dosisMaxima = value; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal value) { this.precioUnitario = value; }
    public List<InventarioEntity> getInventarios() { return inventarios; }
    public List<AplicacionSanitariaEntity> getAplicaciones() { return aplicaciones; }
    public Set<PlanSanitarioEntity> getPlanes() { return planes; }
}
