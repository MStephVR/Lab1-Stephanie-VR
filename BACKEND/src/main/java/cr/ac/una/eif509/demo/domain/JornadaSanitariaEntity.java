package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jornadas_sanitarias")
public class JornadaSanitariaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate fecha;
    private String descripcion;
    @Column(name = "cantidad_animales", nullable = false)
    private Integer cantidadAnimales;
    @Column(name = "costo_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;
    @OneToMany(mappedBy = "jornada", fetch = FetchType.LAZY)
    private List<AplicacionSanitariaEntity> aplicaciones = new ArrayList<>();

    protected JornadaSanitariaEntity() { }
    public JornadaSanitariaEntity(LocalDate fecha, String descripcion, Integer cantidadAnimales, BigDecimal costoTotal, UsuarioEntity usuario) {
        this.fecha = fecha; this.descripcion = descripcion; this.cantidadAnimales = cantidadAnimales; this.costoTotal = costoTotal; this.usuario = usuario;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { this.descripcion = value; }
    public Integer getCantidadAnimales() { return cantidadAnimales; }
    public void setCantidadAnimales(Integer value) { this.cantidadAnimales = value; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal value) { this.costoTotal = value; }
    public UsuarioEntity getUsuario() { return usuario; }
    public void setUsuario(UsuarioEntity value) { this.usuario = value; }
    public List<AplicacionSanitariaEntity> getAplicaciones() { return aplicaciones; }
}
