package cr.ac.una.eif509.demo.presentation;

import cr.ac.una.eif509.demo.business.AplicacionJornadaSalida;
import cr.ac.una.eif509.demo.business.EjecutarJornadaEntrada;
import cr.ac.una.eif509.demo.business.EjecucionJornadaSalida;
import cr.ac.una.eif509.demo.business.JornadaSanitariaService;
import cr.ac.una.eif509.demo.business.PlanificarJornadaEntrada;
import cr.ac.una.eif509.demo.business.RegistroJornadaSalida;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

import java.util.List;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;

@RestController
@RequestMapping("/api/v1/jornadas")
@SecurityRequirement(name = "bearerAuth")
public class JornadaSanitariaController {

    private final JornadaSanitariaService jornadaService;

    public JornadaSanitariaController(JornadaSanitariaService jornadaService) {
        this.jornadaService = jornadaService;
    }

    @GetMapping
    public PagedModel<RegistroJornadaSalida> listar(
            JwtAuthenticationToken authentication,
            @ParameterObject @PageableDefault(size = 20, sort = "fecha",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new PagedModel<>(jornadaService.listarJornadasPropias(authentication.getName(), pageable));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    public ResponseEntity<RegistroJornadaSalida> planificar(
            @Valid @RequestBody PlanificarJornadaEntrada entrada,
            JwtAuthenticationToken authentication
    ) {
        RegistroJornadaSalida salida = jornadaService.planificar(entrada, authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/jornadas/{id}").buildAndExpand(salida.id()).toUri();
        return ResponseEntity.created(location).body(salida);
    }

    @GetMapping("/{jornadaId}")
    public RegistroJornadaSalida consultar(
            @PathVariable Long jornadaId, JwtAuthenticationToken authentication
    ) {
        return jornadaService.consultarJornada(jornadaId, authentication.getName());
    }

    @PostMapping("/{jornadaId}/ejecucion")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    public EjecucionJornadaSalida ejecutar(
            @PathVariable Long jornadaId,
            @Valid @RequestBody EjecutarJornadaEntrada entrada,
            JwtAuthenticationToken authentication
    ) {
        return jornadaService.ejecutar(jornadaId, entrada, authentication.getName());
    }

    @GetMapping("/{jornadaId}/aplicaciones")
    public List<AplicacionJornadaSalida> aplicaciones(
            @PathVariable Long jornadaId, JwtAuthenticationToken authentication
    ) {
        return jornadaService.consultarAplicacionesPorJornada(jornadaId, authentication.getName());
    }
}
