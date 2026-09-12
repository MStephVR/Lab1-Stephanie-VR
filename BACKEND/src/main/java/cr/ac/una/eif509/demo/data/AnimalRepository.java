package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.AnimalEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface AnimalRepository extends BaseRepository<AnimalEntity, Long>, JpaSpecificationExecutor<AnimalEntity> {
	List<AnimalEntity> findByLoteId(Long loteId);

	@Query("select a from AnimalEntity a join fetch a.lote order by a.id")
	List<AnimalEntity> findAllWithLote();
}
