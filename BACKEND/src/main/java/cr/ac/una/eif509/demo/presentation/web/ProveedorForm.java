package cr.ac.una.eif509.demo.presentation.web;

import cr.ac.una.eif509.demo.business.ProveedorEntrada;
import cr.ac.una.eif509.demo.business.ProveedorSalida;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProveedorForm {
    @NotBlank(message = "Este campo es obligatorio.")
    @Size(max = 160, message = "El nombre no puede superar 160 caracteres.")
    private String nombre;

    @Size(max = 40, message = "El teléfono no puede superar 40 caracteres.")
    private String telefono;

    @Email(message = "Ingrese un correo electrónico válido.")
    @Size(max = 160, message = "El correo no puede superar 160 caracteres.")
    private String correo;

    public static ProveedorForm desde(ProveedorSalida proveedor) {
        ProveedorForm form = new ProveedorForm();
        form.setNombre(proveedor.nombre());
        form.setTelefono(proveedor.telefono());
        form.setCorreo(proveedor.correo());
        return form;
    }

    public ProveedorEntrada aEntrada() {
        return new ProveedorEntrada(nombre, telefono, correo);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
