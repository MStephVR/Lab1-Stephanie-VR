package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "alternativas_producto")
public class AlternativaDeProductoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_actual_id", nullable = false)
    private ProductoVeterinarioEntity productoActual;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private ProveedorEntity proveedor;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Double eficacia;

    @Column(name = "plazo_entrega_dias", nullable = false)
    private Integer plazoEntregaDias;

    @Column(nullable = false)
    private boolean activo = true;

    protected AlternativaDeProductoEntity() {
    }

    public AlternativaDeProductoEntity(ProductoVeterinarioEntity productoActual,
                                       ProveedorEntity proveedor,
                                       String nombre,
                                       BigDecimal precioUnitario,
                                       Double eficacia,
                                       Integer plazoEntregaDias) {
        this.productoActual = productoActual;
        this.proveedor = proveedor;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.eficacia = eficacia;
        this.plazoEntregaDias = plazoEntregaDias;
    }

    public Long getId() { return id; }
    public ProductoVeterinarioEntity getProductoActual() { return productoActual; }
    public ProveedorEntity getProveedor() { return proveedor; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public Double getEficacia() { return eficacia; }
    public Integer getPlazoEntregaDias() { return plazoEntregaDias; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}