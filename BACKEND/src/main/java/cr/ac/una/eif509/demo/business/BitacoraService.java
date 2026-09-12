package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.data.BitacoraRepository;
import cr.ac.una.eif509.demo.domain.BitacoraEvento;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Profile({"local", "docker"})
public class BitacoraService {

    private final BitacoraRepository bitacoraRepository;

    public BitacoraService(BitacoraRepository bitacoraRepository) {
        this.bitacoraRepository = bitacoraRepository;
    }

    public List<BitacoraEvento> consultarUltimosEventos() {
        return bitacoraRepository.findAll();
    }

    public BitacoraEvento registrarEvento(String entidad, String accion, String usuario, String detalle) {
        BitacoraEvento evento = new BitacoraEvento(entidad, accion, usuario, detalle, Instant.now());
        return bitacoraRepository.save(evento);
    }
}
