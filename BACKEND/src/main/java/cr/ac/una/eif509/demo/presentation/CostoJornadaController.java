package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.CostoJornada;
import cr.ac.una.eif509.demo.business.CostoJornadaService;
import cr.ac.una.eif509.demo.business.ProcesamientoCostoSalida;
import cr.ac.una.eif509.demo.business.ProcesarCostoJornadaEntrada;
import cr.ac.una.eif509.demo.business.SolicitudCostoJornada;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
public class CostoJornadaController {

    private final CostoJornadaService costoJornadaService;

    public CostoJornadaController(CostoJornadaService costoJornadaService) {
        this.costoJornadaService = costoJornadaService;
    }

    @GetMapping("/costos/jornada")
    public CostoJornada costoJornada(@Valid @ModelAttribute SolicitudCostoJornada solicitud) {
        return costoJornadaService.calcularCostoJornada(solicitud);
    }

    @PostMapping("/jornadas/{jornadaId}/costo")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    public ProcesamientoCostoSalida procesarCostoJornada(
            @PathVariable Long jornadaId,
            @Valid @RequestBody ProcesarCostoJornadaBody body,
            JwtAuthenticationToken authentication) {
        return costoJornadaService.procesarCostoJornada(
                new ProcesarCostoJornadaEntrada(jornadaId, body.manoObra(), body.transporte(),
                        body.veterinario(), body.otros()), authentication.getName());
    }

    public record ProcesarCostoJornadaBody(
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0.00") java.math.BigDecimal manoObra,
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0.00") java.math.BigDecimal transporte,
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0.00") java.math.BigDecimal veterinario,
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0.00") java.math.BigDecimal otros
    ) { }
}