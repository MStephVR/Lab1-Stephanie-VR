package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "aplicaciones_sanitarias")
public class AplicacionSanitariaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private AnimalEntity animal;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private ProductoVeterinarioEntity producto;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jornada_id")
    private JornadaSanitariaEntity jornada;
    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fechaAplicacion;
    @Column(name = "dosis_aplicada", nullable = false)
    private Double dosisAplicada;
    @Column(name = "proxima_fecha_aplicacion")
    private LocalDate proximaFechaAplicacion;
    private String observacion;

    protected AplicacionSanitariaEntity() { }
    public AplicacionSanitariaEntity(AnimalEntity animal, ProductoVeterinarioEntity producto, JornadaSanitariaEntity jornada, LocalDate fechaAplicacion, Double dosisAplicada, LocalDate proximaFechaAplicacion, String observacion) {
        this.animal = animal; this.producto = producto; this.jornada = jornada; this.fechaAplicacion = fechaAplicacion; this.dosisAplicada = dosisAplicada; this.proximaFechaAplicacion = proximaFechaAplicacion; this.observacion = observacion;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AnimalEntity getAnimal() { return animal; }
    public void setAnimal(AnimalEntity value) { this.animal = value; }
    public ProductoVeterinarioEntity getProducto() { return producto; }
    public void setProducto(ProductoVeterinarioEntity value) { this.producto = value; }
    public JornadaSanitariaEntity getJornada() { return jornada; }
    public void setJornada(JornadaSanitariaEntity value) { this.jornada = value; }
    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate value) { this.fechaAplicacion = value; }
    public Double getDosisAplicada() { return dosisAplicada; }
    public void setDosisAplicada(Double value) { this.dosisAplicada = value; }
    public LocalDate getProximaFechaAplicacion() { return proximaFechaAplicacion; }
    public void setProximaFechaAplicacion(LocalDate value) { this.proximaFechaAplicacion = value; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String value) { this.observacion = value; }
}
