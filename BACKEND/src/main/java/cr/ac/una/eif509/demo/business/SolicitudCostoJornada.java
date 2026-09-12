package cr.ac.una.eif509.demo.business;

import java.math.BigDecimal;

public record SolicitudCostoJornada(
        Integer cantidadAnimales,
        Double pesoPromedioKg,
        Double dosisMlPorKg,
        Double contenidoMlPorUnidad,
        BigDecimal precioProducto,
        BigDecimal manoObra,
        BigDecimal transporte,
        BigDecimal veterinario,
        BigDecimal otros
) {
    public SolicitudCostoJornada {
        cantidadAnimales = cantidadAnimales == null ? 1 : cantidadAnimales;
        pesoPromedioKg = pesoPromedioKg == null ? 1 : pesoPromedioKg;
        dosisMlPorKg = dosisMlPorKg == null ? 1 : dosisMlPorKg;
        contenidoMlPorUnidad = contenidoMlPorUnidad == null ? 1 : contenidoMlPorUnidad;
        precioProducto = precioProducto == null ? BigDecimal.ZERO : precioProducto;
        manoObra = manoObra == null ? BigDecimal.ZERO : manoObra;
        transporte = transporte == null ? BigDecimal.ZERO : transporte;
        veterinario = veterinario == null ? BigDecimal.ZERO : veterinario;
        otros = otros == null ? BigDecimal.ZERO : otros;
    }
}