package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class UsuarioEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;
    @Column(nullable = false)
    private String rol;
    @Column(nullable = false)
    private Boolean activo = true;
    @Column(length = 80, unique = true)
    private String username;
    @Column(name = "password_hash", length = 100)
    private String passwordHash;
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<JornadaSanitariaEntity> jornadas = new ArrayList<>();

    protected UsuarioEntity() { }
    public UsuarioEntity(String nombreCompleto, String rol, Boolean activo) {
        this.nombreCompleto = nombreCompleto; this.rol = rol; this.activo = activo;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String value) { this.nombreCompleto = value; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public List<JornadaSanitariaEntity> getJornadas() { return jornadas; }
}
