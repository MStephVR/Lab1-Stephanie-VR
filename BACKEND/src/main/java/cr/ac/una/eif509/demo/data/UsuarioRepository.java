package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.UsuarioEntity;
import java.util.Optional;

public interface UsuarioRepository extends BaseRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByUsername(String username);
}
