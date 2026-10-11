package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.BitacoraService;
import cr.ac.una.eif509.demo.domain.BitacoraEvento;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@Profile({"local", "docker"})
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
public class BitacoraController {

    private final BitacoraService bitacoraService;

    public BitacoraController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping("/bitacora")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BitacoraEvento> listarBitacora() {
        return bitacoraService.consultarUltimosEventos();
    }

    @PostMapping("/bitacora")
    @PreAuthorize("hasRole('ADMIN')")
    public BitacoraEvento registrarEvento(
            @RequestParam String entidad,
            @RequestParam String accion,
            @RequestParam String usuario,
            @RequestParam String detalle
    ) {
        return bitacoraService.registrarEvento(entidad, accion, usuario, detalle);
    }
}
