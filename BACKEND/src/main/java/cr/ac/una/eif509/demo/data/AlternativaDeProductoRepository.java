package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.AlternativaDeProductoEntity;

import java.util.List;

public interface AlternativaDeProductoRepository extends BaseRepository<AlternativaDeProductoEntity, Long> {
    List<AlternativaDeProductoEntity> findByProductoActualIdAndActivoTrueAndEficaciaGreaterThanEqualAndPlazoEntregaDiasLessThanEqual(
            Long productoActualId,
            Double eficaciaMinima,
            Integer plazoEntregaMaximo
    );
}