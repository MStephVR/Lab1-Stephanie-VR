package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.BitacoraService;
import cr.ac.una.eif509.demo.domain.BitacoraEvento;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Profile({"local", "docker"})
@RequestMapping("/api")
public class BitacoraController {

    private final BitacoraService bitacoraService;

    public BitacoraController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping("/bitacora")
    public List<BitacoraEvento> listarBitacora() {
        return bitacoraService.consultarUltimosEventos();
    }

    @PostMapping("/bitacora")
    public BitacoraEvento registrarEvento(
            @RequestParam String entidad,
            @RequestParam String accion,
            @RequestParam String usuario,
            @RequestParam String detalle
    ) {
        return bitacoraService.registrarEvento(entidad, accion, usuario, detalle);
    }
}
