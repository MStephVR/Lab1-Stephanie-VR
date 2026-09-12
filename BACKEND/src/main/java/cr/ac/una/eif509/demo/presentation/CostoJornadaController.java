package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.CostoJornadaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
public class CostoJornadaController {

    private final CostoJornadaService costoJornadaService;

    public CostoJornadaController(CostoJornadaService costoJornadaService) {
        this.costoJornadaService = costoJornadaService;
    }

    @GetMapping("/api/jornadas/costo")
    public Map<String, Object> costoJornada(
            @RequestParam(defaultValue = "1") int animales,
            @RequestParam(defaultValue = "1") double pesoPromedioKg,
            @RequestParam(defaultValue = "1") double dosisMlPorKg,
            @RequestParam(defaultValue = "1") double contenidoMlPorUnidad,
            @RequestParam(defaultValue = "0") BigDecimal precioProducto,
            @RequestParam(defaultValue = "0") BigDecimal manoObra,
            @RequestParam(defaultValue = "0") BigDecimal transporte,
            @RequestParam(defaultValue = "0") BigDecimal veterinario,
            @RequestParam(defaultValue = "0") BigDecimal otros
    ) {
        return costoJornadaService.calcularCostoJornada(
                animales,
                pesoPromedioKg,
                dosisMlPorKg,
                contenidoMlPorUnidad,
                precioProducto,
                manoObra,
                transporte,
                veterinario,
                otros
        );
    }
}