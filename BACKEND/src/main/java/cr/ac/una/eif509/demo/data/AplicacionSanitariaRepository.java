package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.AplicacionSanitariaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AplicacionSanitariaRepository extends BaseRepository<AplicacionSanitariaEntity, Long> {
    List<AplicacionSanitariaEntity> findByAnimalIdOrderByFechaAplicacionDesc(Long animalId);

    @Query("select a from AplicacionSanitariaEntity a join fetch a.animal join fetch a.producto left join fetch a.jornada where a.jornada.id = :jornadaId order by a.fechaAplicacion desc")
    List<AplicacionSanitariaEntity> findByJornadaWithDetails(@Param("jornadaId") Long jornadaId);
}
