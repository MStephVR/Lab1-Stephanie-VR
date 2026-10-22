package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.JornadaConsumoInventarioEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JornadaConsumoInventarioRepository
        extends BaseRepository<JornadaConsumoInventarioEntity, Long> {
    @Query("""
            select c from JornadaConsumoInventarioEntity c
            join fetch c.inventario i
            join fetch i.producto
            where c.jornada.id = :jornadaId
            order by c.id
            """)
    List<JornadaConsumoInventarioEntity> findByJornadaWithInventory(@Param("jornadaId") Long jornadaId);
}
