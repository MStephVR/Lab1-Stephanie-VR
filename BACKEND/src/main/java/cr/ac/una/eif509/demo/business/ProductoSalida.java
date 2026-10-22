package cr.ac.una.eif509.demo.business;

import java.math.BigDecimal;

public record ProductoSalida(Long id, String nombre, String tipo, String unidadMedida,
                             Double dosisMinima, Double dosisMaxima,
                             BigDecimal precioUnitario, Double contenidoMlPorUnidad,
                             Integer retiroCarneDias, Integer retiroLecheDias) { }
