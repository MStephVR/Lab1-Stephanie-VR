package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ProductoVeterinarioRepository extends BaseRepository<ProductoVeterinarioEntity, Long>, JpaSpecificationExecutor<ProductoVeterinarioEntity> {
    List<ProductoVeterinarioEntity> findByTipoIgnoreCase(String tipo);

    @Query("select distinct p from ProductoVeterinarioEntity p left join fetch p.inventarios where p.nombre like :prefijo%")
    List<ProductoVeterinarioEntity> findByNombreStartingWithWithInventario(@Param("prefijo") String prefijo);
}
