package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.RecursoNoEncontradoException;
import cr.ac.una.eif509.demo.business.exception.ConsumoInventarioNoRegistradoException;
import cr.ac.una.eif509.demo.business.exception.DatosCostoJornadaInvalidosException;
import cr.ac.una.eif509.demo.business.exception.JornadaNoCosteableException;
import cr.ac.una.eif509.demo.data.AplicacionSanitariaRepository;
import cr.ac.una.eif509.demo.data.JornadaConsumoInventarioRepository;
import cr.ac.una.eif509.demo.data.JornadaSanitariaRepository;
import cr.ac.una.eif509.demo.data.UsuarioRepository;
import cr.ac.una.eif509.demo.domain.AplicacionSanitariaEntity;
import cr.ac.una.eif509.demo.domain.JornadaConsumoInventarioEntity;
import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import cr.ac.una.eif509.demo.domain.UsuarioEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CostoJornadaService {

    private final CostoProductoStrategy costoProductoStrategy;
    private final JornadaSanitariaRepository jornadaRepository;
    private final AplicacionSanitariaRepository aplicacionRepository;
    private final JornadaConsumoInventarioRepository consumoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CostoProductoStrategy costoPorMililitroStrategy;

    public CostoJornadaService(
            @Qualifier("costoPorUnidadStrategy") CostoProductoStrategy costoProductoStrategy,
            @Qualifier("costoPorMililitroStrategy") CostoProductoStrategy costoPorMililitroStrategy,
            JornadaSanitariaRepository jornadaRepository,
            AplicacionSanitariaRepository aplicacionRepository,
            JornadaConsumoInventarioRepository consumoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.costoProductoStrategy = costoProductoStrategy;
        this.costoPorMililitroStrategy = costoPorMililitroStrategy;
        this.jornadaRepository = jornadaRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.consumoRepository = consumoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ProcesamientoCostoSalida procesarCostoJornada(ProcesarCostoJornadaEntrada entrada) {
        return procesarCostoJornada(entrada, null);
    }

    @Transactional
    public ProcesamientoCostoSalida procesarCostoJornada(
            ProcesarCostoJornadaEntrada entrada, String username
    ) {
        if (entrada == null || entrada.jornadaId() == null || entrada.jornadaId() <= 0) {
            throw new DatosCostoJornadaInvalidosException("La jornada es obligatoria.");
        }
        BigDecimal manoObra = validarCostoAdicional(entrada.manoObra(), "mano de obra");
        BigDecimal transporte = validarCostoAdicional(entrada.transporte(), "transporte");
        BigDecimal veterinario = validarCostoAdicional(entrada.veterinario(), "veterinario");
        BigDecimal otros = validarCostoAdicional(entrada.otros(), "otros gastos");

        JornadaSanitariaEntity jornada = jornadaRepository.findByIdForUpdate(entrada.jornadaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La jornada"));
        verificarPropiedad(jornada, username);
        if (!"EJECUTADA".equals(jornada.getEstado())) {
            throw new JornadaNoCosteableException("Solo se puede procesar el costo de una jornada ejecutada.");
        }

        List<AplicacionSanitariaEntity> aplicaciones =
                aplicacionRepository.findByJornadaWithDetails(jornada.getId());
        if (aplicaciones.isEmpty()) {
            throw new JornadaNoCosteableException("La jornada no tiene aplicaciones sanitarias registradas.");
        }
        Set<Long> productoIds = aplicaciones.stream()
                .map(aplicacion -> aplicacion.getProducto().getId())
                .collect(Collectors.toSet());
        double dosisTotalMl = aplicaciones.stream()
                .mapToDouble(AplicacionSanitariaEntity::getDosisAplicada)
                .sum();
        validarPositivoFinito(dosisTotalMl, "La dosis total de la jornada no es válida.");
        int animales = (int) aplicaciones.stream()
                .map(aplicacion -> aplicacion.getAnimal().getId())
                .distinct()
                .count();
        if (animales <= 0 || animales != jornada.getCantidadAnimales()) {
            throw new JornadaNoCosteableException(
                    "La cantidad de animales aplicados no coincide con la jornada.");
        }

        List<JornadaConsumoInventarioEntity> consumos =
                consumoRepository.findByJornadaWithInventory(jornada.getId());
        if (consumos.isEmpty()) {
            throw new ConsumoInventarioNoRegistradoException(
                    "La ejecución no dejó registrado el inventario consumido; no es posible calcular un costo real.");
        }
        Set<Long> productosConsumidos = consumos.stream()
                .map(consumo -> consumo.getInventario().getProducto().getId())
                .collect(Collectors.toSet());
        if (!productosConsumidos.equals(productoIds)) {
            throw new JornadaNoCosteableException(
                    "Los productos consumidos no coinciden con las aplicaciones de la jornada.");
        }
        int unidades;
        try {
            unidades = Math.toIntExact(consumos.stream()
                    .mapToLong(JornadaConsumoInventarioEntity::getCantidadUnidades).sum());
        } catch (ArithmeticException exception) {
            throw new JornadaNoCosteableException(
                    "La cantidad de unidades consumidas excede el rango permitido.");
        }
        BigDecimal costoProducto = consumos.stream()
                .map(consumo -> costoProductoStrategy.calcularCostoProducto(
                        consumo.getCostoUnitario(), consumo.getCantidadUnidades()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal costosAdicionales = manoObra.add(transporte).add(veterinario).add(otros);
        BigDecimal costoTotal = costoProducto.add(costosAdicionales).setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoPromedio = costoTotal.divide(BigDecimal.valueOf(animales), 2, RoundingMode.HALF_UP);

        jornada.setCostoTotal(costoTotal);
        jornada.setEstado("COSTEADA");
        jornadaRepository.saveAndFlush(jornada);

        return new ProcesamientoCostoSalida(
                jornada.getId(),
                animales,
                redondear(dosisTotalMl),
                unidades,
                costoProducto,
                costosAdicionales,
                costoTotal,
                costoPromedio
        );
    }

    private void verificarPropiedad(JornadaSanitariaEntity jornada, String username) {
        if (username == null) {
            return;
        }
        UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("La jornada"));
        if (jornada.getUsuario() == null || !jornada.getUsuario().getId().equals(usuario.getId())) {
            throw new RecursoNoEncontradoException("La jornada");
        }
    }

    public CostoJornada calcularCostoJornada(SolicitudCostoJornada solicitud) {
        if (solicitud == null) {
            throw new DatosCostoJornadaInvalidosException("La solicitud de costo es requerida.");
        }
        if (solicitud.cantidadAnimales() == null || solicitud.cantidadAnimales() <= 0) {
            throw new DatosCostoJornadaInvalidosException("La cantidad de animales debe ser mayor que cero.");
        }
        validarPositivoFinito(solicitud.pesoPromedioKg(), "El peso promedio debe ser mayor que cero.");
        validarPositivoFinito(solicitud.dosisMlPorKg(), "La dosis por kilogramo debe ser mayor que cero.");
        validarPositivoFinito(solicitud.contenidoMlPorUnidad(), "El contenido por unidad debe ser mayor que cero.");
        if (solicitud.precioProducto() == null || solicitud.precioProducto().signum() < 0) {
            throw new DatosCostoJornadaInvalidosException("El precio del producto no puede ser negativo.");
        }

        BigDecimal manoObra = validarCostoAdicional(solicitud.manoObra(), "mano de obra");
        BigDecimal transporte = validarCostoAdicional(solicitud.transporte(), "transporte");
        BigDecimal veterinario = validarCostoAdicional(solicitud.veterinario(), "veterinario");
        BigDecimal otros = validarCostoAdicional(solicitud.otros(), "otros gastos");

        double dosisPorAnimal = solicitud.pesoPromedioKg() * solicitud.dosisMlPorKg();
        double dosisTotal = dosisPorAnimal * solicitud.cantidadAnimales();
        validarPositivoFinito(dosisTotal, "La dosis total calculada excede el rango permitido.");
        int unidadesRequeridas = costoProductoStrategy.calcularUnidadesRequeridas(
                dosisTotal, solicitud.contenidoMlPorUnidad());
        BigDecimal costoProducto = costoPorMililitroStrategy.calcularCostoProducto(
                solicitud.precioProducto(), dosisTotal, solicitud.contenidoMlPorUnidad());
        BigDecimal costoTotal = costoProducto
                .add(manoObra)
                .add(transporte)
                .add(veterinario)
                .add(otros)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoPromedio = costoTotal.divide(
                BigDecimal.valueOf(solicitud.cantidadAnimales()), 2, RoundingMode.HALF_UP);

        return new CostoJornada(
                solicitud.cantidadAnimales(),
                solicitud.pesoPromedioKg(),
                solicitud.dosisMlPorKg(),
                redondear(dosisPorAnimal),
                redondear(dosisTotal),
                unidadesRequeridas,
                costoProducto,
                manoObra,
                transporte,
                veterinario,
                otros,
                costoTotal,
                costoPromedio
        );
    }

    private BigDecimal validarCostoAdicional(BigDecimal valor, String concepto) {
        if (valor == null || valor.signum() < 0) {
            throw new DatosCostoJornadaInvalidosException(
                    "El costo de " + concepto + " no puede ser nulo ni negativo.");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private void validarPositivoFinito(Double valor, String mensaje) {
        if (valor == null || !Double.isFinite(valor) || valor <= 0) {
            throw new DatosCostoJornadaInvalidosException(mensaje);
        }
    }

    private double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
