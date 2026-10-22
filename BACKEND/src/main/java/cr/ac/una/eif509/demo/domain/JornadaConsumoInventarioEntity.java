package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "jornadas_consumos_inventario",
        uniqueConstraints = @UniqueConstraint(columnNames = {"jornada_id", "inventario_id"}))
public class JornadaConsumoInventarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jornada_id", nullable = false)
    private JornadaSanitariaEntity jornada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private InventarioEntity inventario;

    @Column(name = "cantidad_unidades", nullable = false)
    private Integer cantidadUnidades;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    protected JornadaConsumoInventarioEntity() { }

    public JornadaConsumoInventarioEntity(JornadaSanitariaEntity jornada, InventarioEntity inventario,
                                          Integer cantidadUnidades, BigDecimal costoUnitario) {
        this.jornada = jornada;
        this.inventario = inventario;
        this.cantidadUnidades = cantidadUnidades;
        this.costoUnitario = costoUnitario;
    }

    public Long getId() { return id; }
    public JornadaSanitariaEntity getJornada() { return jornada; }
    public InventarioEntity getInventario() { return inventario; }
    public Integer getCantidadUnidades() { return cantidadUnidades; }
    public BigDecimal getCostoUnitario() { return costoUnitario; }
}
