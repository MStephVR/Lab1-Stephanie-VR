package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "animales")
public class AnimalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigoArete;

    private String raza;
    private String sexo;
    private Double pesoKg;
    private LocalDate fechaNacimiento;
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private LoteEntity lote;

    public AnimalEntity() {
    }

    public AnimalEntity(String codigoArete, String raza, String sexo, Double pesoKg,
                       LocalDate fechaNacimiento, String estado, LoteEntity lote) {
        this.codigoArete = codigoArete;
        this.raza = raza;
        this.sexo = sexo;
        this.pesoKg = pesoKg;
        this.fechaNacimiento = fechaNacimiento;
        this.estado = estado;
        this.lote = lote;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoArete() {
        return codigoArete;
    }

    public void setCodigoArete(String codigoArete) {
        this.codigoArete = codigoArete;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LoteEntity getLote() {
        return lote;
    }

    public void setLote(LoteEntity lote) {
        this.lote = lote;
    }
}
