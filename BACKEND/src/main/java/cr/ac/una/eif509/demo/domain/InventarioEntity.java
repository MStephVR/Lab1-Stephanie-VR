package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventario")
public class InventarioEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private ProductoVeterinarioEntity producto;
    @Column(name = "lote_fabricacion", nullable = false)
    private String loteFabricacion;
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;
    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;
    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    protected InventarioEntity() { }
    public InventarioEntity(ProductoVeterinarioEntity producto, String loteFabricacion, LocalDate fechaVencimiento, Integer cantidadDisponible, BigDecimal costoUnitario) {
        this.producto = producto; this.loteFabricacion = loteFabricacion; this.fechaVencimiento = fechaVencimiento; this.cantidadDisponible = cantidadDisponible; this.costoUnitario = costoUnitario;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProductoVeterinarioEntity getProducto() { return producto; }
    public void setProducto(ProductoVeterinarioEntity value) { this.producto = value; }
    public String getLoteFabricacion() { return loteFabricacion; }
    public void setLoteFabricacion(String value) { this.loteFabricacion = value; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate value) { this.fechaVencimiento = value; }
    public Integer getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(Integer value) { this.cantidadDisponible = value; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal value) { this.costoUnitario = value; }
}
