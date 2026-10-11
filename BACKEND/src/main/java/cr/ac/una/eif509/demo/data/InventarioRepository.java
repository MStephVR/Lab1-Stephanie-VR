package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.InventarioEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface InventarioRepository extends BaseRepository<InventarioEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventarioEntity i where i.producto.id = :productoId and i.fechaVencimiento >= :fecha and i.cantidadDisponible > 0 order by i.fechaVencimiento, i.id")
    List<InventarioEntity> findDisponiblesPorProductoForUpdate(
            @Param("productoId") Long productoId,
            @Param("fecha") LocalDate fecha
    );

    List<InventarioEntity> findByProductoIdAndFechaVencimientoAfterAndCantidadDisponibleGreaterThan(Long productoId, LocalDate fecha, Integer cantidad);

    @Query("select i from InventarioEntity i join fetch i.producto where i.fechaVencimiento >= :hoy and i.cantidadDisponible > 0 order by i.fechaVencimiento")
    List<InventarioEntity> findDisponibleNoVencido(@Param("hoy") LocalDate hoy);
}
