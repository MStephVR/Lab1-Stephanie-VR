package cr.ac.una.eif509.demo.presentation.web;

import cr.ac.una.eif509.demo.business.ProductoEntrada;
import cr.ac.una.eif509.demo.business.ProductoSalida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductoForm {
    @NotBlank(message = "Este campo es obligatorio.")
    @Size(max = 160, message = "El nombre no puede superar 160 caracteres.")
    private String nombre;

    @NotBlank(message = "Este campo es obligatorio.")
    @Size(max = 80, message = "El tipo no puede superar 80 caracteres.")
    private String tipo;

    @NotBlank(message = "Este campo es obligatorio.")
    @Size(max = 30, message = "La unidad no puede superar 30 caracteres.")
    private String unidadMedida;

    @DecimalMin(value = "0.0", message = "La dosis mínima no puede ser negativa.")
    private Double dosisMinima;

    @DecimalMin(value = "0.0", message = "La dosis máxima no puede ser negativa.")
    private Double dosisMaxima;

    @NotNull(message = "Este campo es obligatorio.")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 10 enteros y 2 decimales.")
    private BigDecimal precioUnitario;

    @NotNull(message = "Este campo es obligatorio.")
    @Positive(message = "El contenido debe ser mayor que cero.")
    private Double contenidoMlPorUnidad;
    @PositiveOrZero(message = "El retiro de carne no puede ser negativo.")
    private Integer retiroCarneDias = 0;
    @PositiveOrZero(message = "El retiro de leche no puede ser negativo.")
    private Integer retiroLecheDias = 0;

    public static ProductoForm desde(ProductoSalida producto) {
        ProductoForm form = new ProductoForm();
        form.setNombre(producto.nombre());
        form.setTipo(producto.tipo());
        form.setUnidadMedida(producto.unidadMedida());
        form.setDosisMinima(producto.dosisMinima());
        form.setDosisMaxima(producto.dosisMaxima());
        form.setPrecioUnitario(producto.precioUnitario());
        form.setContenidoMlPorUnidad(producto.contenidoMlPorUnidad());
        form.setRetiroCarneDias(producto.retiroCarneDias());
        form.setRetiroLecheDias(producto.retiroLecheDias());
        return form;
    }

    public ProductoEntrada aEntrada() {
        return new ProductoEntrada(nombre, tipo, unidadMedida, dosisMinima, dosisMaxima,
                precioUnitario, contenidoMlPorUnidad, retiroCarneDias, retiroLecheDias);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public Double getDosisMinima() { return dosisMinima; }
    public void setDosisMinima(Double dosisMinima) { this.dosisMinima = dosisMinima; }
    public Double getDosisMaxima() { return dosisMaxima; }
    public void setDosisMaxima(Double dosisMaxima) { this.dosisMaxima = dosisMaxima; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public Double getContenidoMlPorUnidad() { return contenidoMlPorUnidad; }
    public void setContenidoMlPorUnidad(Double contenidoMlPorUnidad) {
        this.contenidoMlPorUnidad = contenidoMlPorUnidad;
    }
    public Integer getRetiroCarneDias() { return retiroCarneDias; }
    public void setRetiroCarneDias(Integer retiroCarneDias) { this.retiroCarneDias = retiroCarneDias; }
    public Integer getRetiroLecheDias() { return retiroLecheDias; }
    public void setRetiroLecheDias(Integer retiroLecheDias) { this.retiroLecheDias = retiroLecheDias; }
}
