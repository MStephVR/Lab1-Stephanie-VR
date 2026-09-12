package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.CompraEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface CompraRepository extends BaseRepository<CompraEntity, Long> {
    List<CompraEntity> findByProveedorIdAndFechaCompraBetween(Long proveedorId, LocalDate desde, LocalDate hasta);

    @Query("select c from CompraEntity c join fetch c.proveedor where c.fechaCompra between :desde and :hasta order by c.fechaCompra desc")
    List<CompraEntity> findByFechaBetweenWithProveedor(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
