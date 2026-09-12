package cr.ac.una.eif509.demo.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proveedores")
public class ProveedorEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String nombre;
    private String telefono;
    private String correo;
    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY)
    private List<CompraEntity> compras = new ArrayList<>();

    protected ProveedorEntity() { }
    public ProveedorEntity(String nombre, String telefono, String correo) {
        this.nombre = nombre; this.telefono = telefono; this.correo = correo;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public List<CompraEntity> getCompras() { return compras; }
}
