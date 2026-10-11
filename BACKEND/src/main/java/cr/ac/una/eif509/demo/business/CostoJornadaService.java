package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.ReglaNegocioException;
import cr.ac.una.eif509.demo.business.exception.InventarioInsuficienteException;
import cr.ac.una.eif509.demo.business.exception.RecursoNoEncontradoException;
import cr.ac.una.eif509.demo.data.AplicacionSanitariaRepository;
import cr.ac.una.eif509.demo.data.InventarioRepository;
import cr.ac.una.eif509.demo.data.JornadaSanitariaRepository;
import cr.ac.una.eif509.demo.data.UsuarioRepository;
import cr.ac.una.eif509.demo.domain.AplicacionSanitariaEntity;
import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import cr.ac.una.eif509.demo.domain.UsuarioEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CostoJornadaService {

    private final CostoProductoStrategy costoProductoStrategy;
    private final JornadaSanitariaRepository jornadaRepository;
    private final AplicacionSanitariaRepository aplicacionRepository;
    private final InventarioRepository inventarioRepository;
    private final UsuarioRepository usuarioRepository;

    public CostoJornadaService(
            CostoProductoStrategy costoProductoStrategy,
            JornadaSanitariaRepository jornadaRepository,
            AplicacionSanitariaRepository aplicacionRepository,
            InventarioRepository inventarioRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.costoProductoStrategy = costoProductoStrategy;
        this.jornadaRepository = jornadaRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.inventarioRepository = inventarioRepository;
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
            throw new ReglaNegocioException("La jornada es obligatoria.");
        }
        BigDecimal manoObra = validarCostoAdicional(entrada.manoObra(), "mano de obra");
        BigDecimal transporte = validarCostoAdicional(entrada.transporte(), "transporte");
        BigDecimal veterinario = validarCostoAdicional(entrada.veterinario(), "veterinario");
        BigDecimal otros = validarCostoAdicional(entrada.otros(), "otros gastos");

        JornadaSanitariaEntity jornada = jornadaRepository.findByIdForUpdate(entrada.jornadaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La jornada"));
        verificarPropiedad(jornada, username);
        if (!"EJECUTADA".equals(jornada.getEstado())) {
            throw new ReglaNegocioException("Solo se puede procesar el costo de una jornada ejecutada.");
        }

        List<AplicacionSanitariaEntity> aplicaciones =
                aplicacionRepository.findByJornadaWithDetails(jornada.getId());
        if (aplicaciones.isEmpty()) {
            throw new ReglaNegocioException("La jornada no tiene aplicaciones sanitarias registradas.");
        }
        Set<Long> productoIds = aplicaciones.stream()
                .map(aplicacion -> aplicacion.getProducto().getId())
                .collect(Collectors.toSet());
        if (productoIds.size() != 1) {
            throw new ReglaNegocioException(
                    "El cálculo de costo requiere que todas las aplicaciones usen un producto.");
        }

        Long productoId = productoIds.iterator().next();
        List<InventarioEntity> inventarios = inventarioRepository.findDisponiblesPorProductoForUpdate(
                productoId, LocalDate.now());
        if (inventarios.isEmpty()) {
            throw new ReglaNegocioException("No hay inventario vigente disponible para el producto aplicado.");
        }
        validarPositivoFinito(inventarios.get(0).getProducto().getContenidoMlPorUnidad(),
                "El contenido por unidad del producto no es válido.");

        double dosisTotalMl = aplicaciones.stream()
                .mapToDouble(AplicacionSanitariaEntity::getDosisAplicada)
                .sum();
        validarPositivoFinito(dosisTotalMl, "La dosis total de la jornada no es válida.");
        int animales = (int) aplicaciones.stream()
                .map(aplicacion -> aplicacion.getAnimal().getId())
                .distinct()
                .count();
        if (animales <= 0 || animales != jornada.getCantidadAnimales()) {
            throw new ReglaNegocioException("La cantidad de animales aplicados no coincide con la jornada.");
        }

        int unidades = costoProductoStrategy.calcularUnidadesRequeridas(
                dosisTotalMl, inventarios.get(0).getProducto().getContenidoMlPorUnidad());
        long disponibles = inventarios.stream().mapToLong(InventarioEntity::getCantidadDisponible).sum();
        if (disponibles < unidades) {
            throw new InventarioInsuficienteException(
                    inventarios.get(0).getProducto().getNombre(), disponibles, unidades);
        }
        BigDecimal costoProducto = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int unidadesPendientes = unidades;
        for (InventarioEntity inventario : inventarios) {
            int unidadesUsadas = Math.min(unidadesPendientes, inventario.getCantidadDisponible());
            if (unidadesUsadas == 0) {
                break;
            }
            costoProducto = costoProducto.add(costoProductoStrategy.calcularCostoProducto(
                    inventario.getCostoUnitario(), unidadesUsadas));
            inventario.setCantidadDisponible(inventario.getCantidadDisponible() - unidadesUsadas);
            unidadesPendientes -= unidadesUsadas;
        }
        BigDecimal costosAdicionales = manoObra.add(transporte).add(veterinario).add(otros);
        BigDecimal costoTotal = costoProducto.add(costosAdicionales).setScale(2, RoundingMode.HALF_UP);
        BigDecimal costoPromedio = costoTotal.divide(BigDecimal.valueOf(animales), 2, RoundingMode.HALF_UP);

        jornada.setCostoTotal(costoTotal);
        jornada.setEstado("COSTEADA");
        jornadaRepository.saveAndFlush(jornada);

        inventarioRepository.saveAll(inventarios);

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
            throw new ReglaNegocioException("La solicitud de costo es requerida.");
        }
        if (solicitud.cantidadAnimales() == null || solicitud.cantidadAnimales() <= 0) {
            throw new ReglaNegocioException("La cantidad de animales debe ser mayor que cero.");
        }
        validarPositivoFinito(solicitud.pesoPromedioKg(), "El peso promedio debe ser mayor que cero.");
        validarPositivoFinito(solicitud.dosisMlPorKg(), "La dosis por kilogramo debe ser mayor que cero.");
        validarPositivoFinito(solicitud.contenidoMlPorUnidad(), "El contenido por unidad debe ser mayor que cero.");
        if (solicitud.precioProducto() == null || solicitud.precioProducto().signum() < 0) {
            throw new ReglaNegocioException("El precio del producto no puede ser negativo.");
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
        BigDecimal costoProducto = costoProductoStrategy.calcularCostoProducto(
                solicitud.precioProducto(), unidadesRequeridas);
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
            throw new ReglaNegocioException("El costo de " + concepto + " no puede ser nulo ni negativo.");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private void validarPositivoFinito(Double valor, String mensaje) {
        if (valor == null || !Double.isFinite(valor) || valor <= 0) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    private double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
