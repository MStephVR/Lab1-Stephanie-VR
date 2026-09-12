package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.BitacoraEvento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraRepository extends MongoRepository<BitacoraEvento, String> {
}
