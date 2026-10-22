package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.RecursoNoEncontradoException;
import cr.ac.una.eif509.demo.data.BitacoraRepository;
import cr.ac.una.eif509.demo.domain.BitacoraEvento;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Profile({"local", "docker", "test"})
public class BitacoraService {

    private final BitacoraRepository bitacoraRepository;

    public BitacoraService(BitacoraRepository bitacoraRepository) {
        this.bitacoraRepository = bitacoraRepository;
    }

    public List<BitacoraSalida> consultarUltimosEventos() {
        return bitacoraRepository.findAll().stream().map(this::salida).toList();
    }

    public BitacoraSalida consultarEvento(String id) {
        return bitacoraRepository.findById(id)
                .map(this::salida)
                .orElseThrow(() -> new RecursoNoEncontradoException("El evento de bitácora"));
    }

    public BitacoraSalida registrarEvento(BitacoraEntrada entrada) {
        BitacoraEvento evento = new BitacoraEvento(
                entrada.entidad(), entrada.accion(), entrada.usuario(), entrada.detalle(), Instant.now());
        return salida(bitacoraRepository.save(evento));
    }

    private BitacoraSalida salida(BitacoraEvento evento) {
        return new BitacoraSalida(evento.getId(), evento.getEntidad(), evento.getAccion(),
                evento.getUsuario(), evento.getDetalle(), evento.getFechaHora());
    }
}
