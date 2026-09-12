package cr.ac.una.eif509.demo.business;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CostoJornadaService {

    public Map<String, Object> calcularCostoJornada(
            int cantidadAnimales,
            double pesoPromedioKg,
            double dosisMlPorKg,
            double contenidoMlPorUnidad,
            BigDecimal precioProducto,
            BigDecimal manoObra,
            BigDecimal transporte,
            BigDecimal veterinario,
            BigDecimal otros
    ) {
        int animales = Math.max(1, cantidadAnimales);
        double dosisPorAnimal = pesoPromedioKg * dosisMlPorKg;
        double dosisTotal = dosisPorAnimal * animales;

        double unidadesRequeridas = contenidoMlPorUnidad <= 0 ? 0 : Math.ceil(dosisTotal / contenidoMlPorUnidad);

        BigDecimal costoProducto = precioProducto.multiply(BigDecimal.valueOf(unidadesRequeridas))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoTotal = costoProducto
                .add(manoObra)
                .add(transporte)
                .add(veterinario)
                .add(otros)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoPromedio = costoTotal.divide(BigDecimal.valueOf(animales), 2, RoundingMode.HALF_UP);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("cantidadAnimales", animales);
        resultado.put("pesoPromedioKg", pesoPromedioKg);
        resultado.put("dosisMlPorKg", dosisMlPorKg);
        resultado.put("dosisPorAnimal", redondear(dosisPorAnimal));
        resultado.put("dosisTotal", redondear(dosisTotal));
        resultado.put("unidadesRequeridas", redondear(unidadesRequeridas));
        resultado.put("costoProducto", costoProducto);
        resultado.put("manoObra", manoObra);
        resultado.put("transporte", transporte);
        resultado.put("veterinario", veterinario);
        resultado.put("otros", otros);
        resultado.put("costoTotal", costoTotal);
        resultado.put("costoPromedioPorAnimal", costoPromedio);
        return resultado;
    }

    private double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}