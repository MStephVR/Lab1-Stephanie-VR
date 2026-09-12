package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.CostoJornada;
import cr.ac.una.eif509.demo.business.CostoJornadaService;
import cr.ac.una.eif509.demo.business.SolicitudCostoJornada;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CostoJornadaController {

    private final CostoJornadaService costoJornadaService;

    public CostoJornadaController(CostoJornadaService costoJornadaService) {
        this.costoJornadaService = costoJornadaService;
    }

    @GetMapping("/api/jornadas/costo")
    public CostoJornada costoJornada(@ModelAttribute SolicitudCostoJornada solicitud) {
        return costoJornadaService.calcularCostoJornada(solicitud);
    }
}