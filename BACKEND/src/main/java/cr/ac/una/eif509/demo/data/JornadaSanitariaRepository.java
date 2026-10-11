package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JornadaSanitariaRepository extends BaseRepository<JornadaSanitariaEntity, Long> {
    Page<JornadaSanitariaEntity> findByUsuarioUsername(String username, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from JornadaSanitariaEntity j where j.id = :id")
    Optional<JornadaSanitariaEntity> findByIdForUpdate(@Param("id") Long id);

    @Query("select j from JornadaSanitariaEntity j join fetch j.usuario where j.fecha between :desde and :hasta order by j.fecha")
    List<JornadaSanitariaEntity> findByFechaBetweenWithUsuario(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
