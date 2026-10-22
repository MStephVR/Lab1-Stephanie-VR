package cr.ac.una.eif509.demo.business;

import java.time.Instant;

public record BitacoraSalida(
        String id,
        String entidad,
        String accion,
        String usuario,
        String detalle,
        Instant fechaHora
) { }
