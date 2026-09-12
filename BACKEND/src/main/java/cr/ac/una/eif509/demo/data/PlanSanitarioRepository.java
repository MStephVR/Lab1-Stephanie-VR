package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.PlanSanitarioEntity;
import java.util.List;

public interface PlanSanitarioRepository extends BaseRepository<PlanSanitarioEntity, Long> {
    List<PlanSanitarioEntity> findByEdadObjetivoMesesBetween(Integer minimo, Integer maximo);
}
