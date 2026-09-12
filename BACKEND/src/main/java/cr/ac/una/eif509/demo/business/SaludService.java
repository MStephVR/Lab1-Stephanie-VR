package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.data.SaludRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CAPA DE NEGOCIO.
 * Aquí vive la lógica y las reglas del sistema de control de ganado.
 * NO sabe que existe HTTP ni un navegador: solo resuelve el problema de negocio.
 *
 * Dirección de la dependencia: negocio --> datos.
 * El servicio conoce al repositorio, pero el repositorio NO conoce al servicio.
 */
@Service
public class SaludService {

    private final SaludRepository saludRepository;

    public SaludService(SaludRepository saludRepository) {
        this.saludRepository = saludRepository;
    }

    public Map<String, Object> resumenDelControl(int cantidadAnimales) {
        int animales = Math.max(1, cantidadAnimales);
        int costoVacunacion = animales * saludRepository.costoVacunacionPorAnimal();
        int costoDesparasitacion = animales * saludRepository.costoDesparasitacionPorAnimal();
        int costoTotal = costoVacunacion + costoDesparasitacion;

        return Map.of(
                "mensaje", saludRepository.leerEstadoDelSistema(),
                "cantidadAnimales", animales,
                "costoVacunacion", costoVacunacion,
                "costoDesparasitacion", costoDesparasitacion,
                "costoTotal", costoTotal
        );
    }
}
