package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.PresupuestoJornadaEntity;

import java.time.LocalDate;
import java.util.List;

public interface PresupuestoJornadaRepository extends BaseRepository<PresupuestoJornadaEntity, Long> {
    List<PresupuestoJornadaEntity> findByFechaPresupuestoBetween(LocalDate desde, LocalDate hasta);
}