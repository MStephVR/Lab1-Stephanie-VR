package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface JornadaSanitariaRepository extends BaseRepository<JornadaSanitariaEntity, Long> {
    @Query("select j from JornadaSanitariaEntity j join fetch j.usuario where j.fecha between :desde and :hasta order by j.fecha")
    List<JornadaSanitariaEntity> findByFechaBetweenWithUsuario(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
