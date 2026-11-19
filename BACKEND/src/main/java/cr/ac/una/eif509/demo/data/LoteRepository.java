package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.LoteEntity;
import java.util.Optional;

public interface LoteRepository extends BaseRepository<LoteEntity, Long> {
    Optional<LoteEntity> findByNombre(String nombre);
}
