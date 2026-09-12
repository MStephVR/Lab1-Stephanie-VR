package cr.ac.una.eif509.demo.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "bitacora_eventos")
public class BitacoraEvento {

    @Id
    private String id;

    private String entidad;
    private String accion;
    private String usuario;
    private String detalle;
    private Instant fechaHora;

    public BitacoraEvento() {
    }

    public BitacoraEvento(String entidad, String accion, String usuario, String detalle, Instant fechaHora) {
        this.entidad = entidad;
        this.accion = accion;
        this.usuario = usuario;
        this.detalle = detalle;
        this.fechaHora = fechaHora;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }
}
