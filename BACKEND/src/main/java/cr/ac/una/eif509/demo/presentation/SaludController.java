package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.SaludService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * CAPA DE PRESENTACIÓN.
 * Su única responsabilidad es recibir peticiones HTTP y devolver respuestas.
 * NO contiene lógica de negocio: para eso le pregunta a la capa de negocio (SaludService).
 *
 * Fijate en la dirección de la dependencia: presentación --> negocio.
 * El controlador conoce al servicio, pero el servicio NO conoce al controlador.
 */
@RestController
public class SaludController {

    private final SaludService saludService;

    // Spring "inyecta" automáticamente el servicio al construir el controlador.
    public SaludController(SaludService saludService) {
        this.saludService = saludService;
    }

    @GetMapping("/api/ganado/salud")
    public Map<String, Object> controlDeGanado(@RequestParam(defaultValue = "1") int animales) {
        // El controlador delega: le pide el resumen a la capa de negocio.
        return saludService.resumenDelControl(animales);
    }
}
