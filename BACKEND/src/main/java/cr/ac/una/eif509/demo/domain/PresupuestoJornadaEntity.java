package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "presupuestos_jornada")
public class PresupuestoJornadaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_presupuesto", nullable = false)
    private LocalDate fechaPresupuesto;

    @Column(name = "cantidad_animales", nullable = false)
    private Integer cantidadAnimales;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_actual_id", nullable = false)
    private ProductoVeterinarioEntity productoActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jornada_id")
    private JornadaSanitariaEntity jornada;

    @Column(name = "costo_producto", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoProducto;

    @Column(name = "costos_adicionales", nullable = false, precision = 14, scale = 2)
    private BigDecimal costosAdicionales;

    @Column(name = "costo_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoTotal;

    protected PresupuestoJornadaEntity() {
    }

    public PresupuestoJornadaEntity(LocalDate fechaPresupuesto,
                                    Integer cantidadAnimales,
                                    ProductoVeterinarioEntity productoActual,
                                    JornadaSanitariaEntity jornada,
                                    BigDecimal costoProducto,
                                    BigDecimal costosAdicionales,
                                    BigDecimal costoTotal) {
        this.fechaPresupuesto = fechaPresupuesto;
        this.cantidadAnimales = cantidadAnimales;
        this.productoActual = productoActual;
        this.jornada = jornada;
        this.costoProducto = costoProducto;
        this.costosAdicionales = costosAdicionales;
        this.costoTotal = costoTotal;
    }

    public Long getId() { return id; }
    public LocalDate getFechaPresupuesto() { return fechaPresupuesto; }
    public Integer getCantidadAnimales() { return cantidadAnimales; }
    public ProductoVeterinarioEntity getProductoActual() { return productoActual; }
    public JornadaSanitariaEntity getJornada() { return jornada; }
    public BigDecimal getCostoProducto() { return costoProducto; }
    public BigDecimal getCostosAdicionales() { return costosAdicionales; }
    public BigDecimal getCostoTotal() { return costoTotal; }
}