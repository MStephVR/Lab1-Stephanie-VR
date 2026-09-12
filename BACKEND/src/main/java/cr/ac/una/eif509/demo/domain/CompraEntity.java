package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "compras")
public class CompraEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private ProveedorEntity proveedor;
    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    protected CompraEntity() { }
    public CompraEntity(ProveedorEntity proveedor, LocalDate fechaCompra, BigDecimal total) {
        this.proveedor = proveedor; this.fechaCompra = fechaCompra; this.total = total;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProveedorEntity getProveedor() { return proveedor; }
    public void setProveedor(ProveedorEntity value) { this.proveedor = value; }
    public LocalDate getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDate value) { this.fechaCompra = value; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal value) { this.total = value; }
}
