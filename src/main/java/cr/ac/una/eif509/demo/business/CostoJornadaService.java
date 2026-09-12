package cr.ac.una.eif509.demo.business;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CostoJornadaService {

    public record SolicitudCostoJornada(
                        Integer animales,
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
                        animales = animales == null ? 1 : animales;
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

    public record CostoJornada(
            int cantidadAnimales,
            double pesoPromedioKg,
            double dosisMlPorKg,
            double dosisPorAnimal,
            double dosisTotal,
            double unidadesRequeridas,
            BigDecimal costoProducto,
            BigDecimal manoObra,
            BigDecimal transporte,
            BigDecimal veterinario,
            BigDecimal otros,
            BigDecimal costoTotal,
            BigDecimal costoPromedioPorAnimal
    ) { }

    public CostoJornada calcularCostoJornada(SolicitudCostoJornada solicitud) {
                int animales = Math.max(1, solicitud.animales());
        double dosisPorAnimal = solicitud.pesoPromedioKg() * solicitud.dosisMlPorKg();
        double dosisTotal = dosisPorAnimal * animales;
        double unidadesRequeridas = solicitud.contenidoMlPorUnidad() <= 0
                ? 0
                : dosisTotal / solicitud.contenidoMlPorUnidad();

        BigDecimal costoProducto = solicitud.precioProducto().multiply(BigDecimal.valueOf(unidadesRequeridas))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoTotal = costoProducto
                .add(solicitud.manoObra())
                .add(solicitud.transporte())
                .add(solicitud.veterinario())
                .add(solicitud.otros())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoPromedio = costoTotal.divide(BigDecimal.valueOf(animales), 2, RoundingMode.HALF_UP);

        return new CostoJornada(
                animales,
                solicitud.pesoPromedioKg(),
                solicitud.dosisMlPorKg(),
                redondear(dosisPorAnimal),
                redondear(dosisTotal),
                redondear(unidadesRequeridas),
                costoProducto,
                solicitud.manoObra(),
                solicitud.transporte(),
                solicitud.veterinario(),
                solicitud.otros(),
                costoTotal,
                costoPromedio
        );
    }

    private double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}