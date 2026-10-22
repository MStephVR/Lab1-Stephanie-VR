package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.BitacoraService;
import cr.ac.una.eif509.demo.business.BitacoraEntrada;
import cr.ac.una.eif509.demo.business.BitacoraSalida;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Profile({"local", "docker", "prod", "test"})
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
public class BitacoraController {

    private final BitacoraService bitacoraService;

    public BitacoraController(BitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @GetMapping("/bitacora")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BitacoraSalida> listarBitacora() {
        return bitacoraService.consultarUltimosEventos();
    }

    @GetMapping("/bitacora/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BitacoraSalida consultarEvento(@PathVariable String id) {
        return bitacoraService.consultarEvento(id);
    }

    @PostMapping("/bitacora")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BitacoraSalida> registrarEvento(@Valid @RequestBody BitacoraEntrada entrada) {
        BitacoraSalida salida = bitacoraService.registrarEvento(entrada);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/bitacora/{id}").buildAndExpand(salida.id()).toUri();
        return ResponseEntity.created(location).body(salida);
    }
}
