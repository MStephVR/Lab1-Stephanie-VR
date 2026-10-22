package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.InventarioInsuficienteException;
import cr.ac.una.eif509.demo.business.exception.JornadaNoEjecutableException;
import cr.ac.una.eif509.demo.business.exception.RecursoNoEncontradoException;
import cr.ac.una.eif509.demo.business.exception.DatosJornadaInvalidosException;
import cr.ac.una.eif509.demo.business.exception.DosisFueraDeRangoException;
import cr.ac.una.eif509.demo.business.exception.IntervaloMinimoNoCumplidoException;
import cr.ac.una.eif509.demo.business.exception.LoteInactivoException;
import cr.ac.una.eif509.demo.business.exception.PlanSanitarioSinProductosException;
import cr.ac.una.eif509.demo.data.AnimalRepository;
import cr.ac.una.eif509.demo.data.AplicacionSanitariaRepository;
import cr.ac.una.eif509.demo.data.InventarioRepository;
import cr.ac.una.eif509.demo.data.JornadaConsumoInventarioRepository;
import cr.ac.una.eif509.demo.data.JornadaSanitariaRepository;
import cr.ac.una.eif509.demo.data.LoteRepository;
import cr.ac.una.eif509.demo.data.PlanSanitarioRepository;
import cr.ac.una.eif509.demo.data.ProductoVeterinarioRepository;
import cr.ac.una.eif509.demo.data.UsuarioRepository;
import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.AplicacionSanitariaEntity;
import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.JornadaConsumoInventarioEntity;
import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import cr.ac.una.eif509.demo.domain.LoteEntity;
import cr.ac.una.eif509.demo.domain.PlanSanitarioEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import cr.ac.una.eif509.demo.domain.UsuarioEntity;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JornadaSanitariaService {

    private final JornadaSanitariaRepository jornadaRepository;
    private final AplicacionSanitariaRepository aplicacionRepository;
    private final ProductoVeterinarioRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final JornadaConsumoInventarioRepository consumoRepository;
    private final LoteRepository loteRepository;
    private final PlanSanitarioRepository planRepository;
    private final AnimalRepository animalRepository;
    private final JornadaSanitariaFactory jornadaFactory;
    private final CostoProductoStrategy costoProductoStrategy;
    private final UsuarioRepository usuarioRepository;

    public JornadaSanitariaService(
            JornadaSanitariaRepository jornadaRepository,
            AplicacionSanitariaRepository aplicacionRepository,
            ProductoVeterinarioRepository productoRepository,
            InventarioRepository inventarioRepository,
            JornadaConsumoInventarioRepository consumoRepository,
            LoteRepository loteRepository,
            PlanSanitarioRepository planRepository,
            AnimalRepository animalRepository,
            JornadaSanitariaFactory jornadaFactory,
            CostoProductoStrategy costoProductoStrategy,
            UsuarioRepository usuarioRepository
    ) {
        this.jornadaRepository = jornadaRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.consumoRepository = consumoRepository;
        this.loteRepository = loteRepository;
        this.planRepository = planRepository;
        this.animalRepository = animalRepository;
        this.jornadaFactory = jornadaFactory;
        this.costoProductoStrategy = costoProductoStrategy;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public RegistroJornadaSalida planificar(@Valid PlanificarJornadaEntrada entrada) {
        return planificar(entrada, null);
    }

    @Transactional
    public RegistroJornadaSalida planificar(@Valid PlanificarJornadaEntrada entrada, String username) {
        if (entrada == null || entrada.loteId() == null || entrada.loteId() <= 0
                || entrada.planId() == null || entrada.planId() <= 0
                || entrada.fecha() == null || entrada.fecha().isBefore(LocalDate.now())
                || entrada.descripcion() == null || entrada.descripcion().isBlank()
                || entrada.descripcion().length() > 200) {
            throw new DatosJornadaInvalidosException(
                    "Los datos de planificación de la jornada no son válidos.");
        }

        LoteEntity lote = loteRepository.findById(entrada.loteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El lote"));
        if (!lote.isActivo()) {
            throw new LoteInactivoException("No se puede planificar una jornada para un lote inactivo.");
        }
        PlanSanitarioEntity plan = planRepository.findById(entrada.planId())
                .orElseThrow(() -> new RecursoNoEncontradoException("El plan sanitario"));
        if (plan.getProductos().isEmpty()) {
            throw new PlanSanitarioSinProductosException(
                    "El plan sanitario debe tener al menos un producto asociado.");
        }

        List<AnimalEntity> animalesActivos = animalRepository.findByLoteId(lote.getId()).stream()
                .filter(animal -> "Activo".equalsIgnoreCase(Objects.toString(animal.getEstado(), "")))
                .toList();
        if (animalesActivos.isEmpty()) {
            throw new DatosJornadaInvalidosException(
                    "No se puede planificar una jornada para un lote sin animales activos.");
        }
        int intervaloMinimoDias = intervaloMinimo(plan);
        validarIntervaloMinimo(animalesActivos, plan, entrada.fecha(), intervaloMinimoDias);

        JornadaSanitariaEntity jornada = jornadaFactory.crearJornada(
                entrada,
                animalesActivos.size(),
                lote,
                plan
        );
        if (username != null) {
            UsuarioEntity usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RecursoNoEncontradoException("El usuario"));
            jornada.setUsuario(usuario);
        }
        JornadaSanitariaEntity guardada = jornadaRepository.save(jornada);
        return new RegistroJornadaSalida(
                guardada.getId(),
                lote.getId(),
                plan.getId(),
                guardada.getFecha(),
                guardada.getDescripcion(),
                guardada.getCantidadAnimales(),
                guardada.getCostoTotal(),
                "Jornada planificada"
        );
    }

    @Transactional
    public EjecucionJornadaSalida ejecutar(Long jornadaId, @Valid EjecutarJornadaEntrada entrada) {
        return ejecutar(jornadaId, entrada, null);
    }

    @Transactional
    public EjecucionJornadaSalida ejecutar(
            Long jornadaId, @Valid EjecutarJornadaEntrada entrada, String username
    ) {
        if (jornadaId == null || jornadaId <= 0 || entrada == null
                || entrada.animales() == null || entrada.animales().isEmpty()
                || entrada.productos() == null || entrada.productos().isEmpty()) {
            throw new DatosJornadaInvalidosException(
                    "La jornada debe incluir animales y productos para ejecutarse.");
        }

        JornadaSanitariaEntity jornada = jornadaRepository.findByIdForUpdate(jornadaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La jornada"));
        verificarPropiedad(jornada, username);
        if (!"PLANIFICADA".equals(jornada.getEstado())) {
            throw new JornadaNoEjecutableException(jornada.getEstado());
        }
        if (jornada.getFecha().isAfter(LocalDate.now())) {
            throw new DatosJornadaInvalidosException(
                    "La jornada no se puede ejecutar antes de su fecha planificada.");
        }

        List<Long> animalIds = entrada.animales();
        if (animalIds.stream().anyMatch(Objects::isNull)
                || animalIds.stream().anyMatch(id -> id <= 0)
                || animalIds.size() != new HashSet<>(animalIds).size()) {
            throw new DatosJornadaInvalidosException(
                    "Los animales de la ejecución deben ser IDs válidos y no repetidos.");
        }
        List<AnimalEntity> animalesDelLote = animalRepository.findByLoteId(jornada.getLote().getId()).stream()
                .filter(animal -> "Activo".equalsIgnoreCase(Objects.toString(animal.getEstado(), "")))
                .toList();
        Set<Long> animalesEsperados = animalesDelLote.stream()
                .map(AnimalEntity::getId)
                .collect(Collectors.toSet());
        if (animalesEsperados.isEmpty() || !animalesEsperados.equals(new HashSet<>(animalIds))) {
            throw new DatosJornadaInvalidosException(
                    "Deben registrarse exactamente una vez todos los animales activos del lote.");
        }
        for (AnimalEntity animal : animalesDelLote) {
            if (animal.getPesoKg() == null || !Double.isFinite(animal.getPesoKg()) || animal.getPesoKg() <= 0) {
                throw new DatosJornadaInvalidosException(
                        "El animal " + animal.getId() + " no tiene un peso válido para calcular su dosis.");
            }
        }

        List<RegistroProductoJornada> detalleProductos = entrada.productos();
        List<Long> productoIdsEntrada = detalleProductos.stream()
                .filter(Objects::nonNull)
                .map(RegistroProductoJornada::productoId)
                .filter(Objects::nonNull)
                .toList();
        if (productoIdsEntrada.size() != detalleProductos.size()
                || productoIdsEntrada.stream().anyMatch(id -> id <= 0)
                || productoIdsEntrada.size() != new HashSet<>(productoIdsEntrada).size()) {
            throw new DatosJornadaInvalidosException("Los productos deben ser válidos y no repetirse.");
        }

        Map<Long, ProductoVeterinarioEntity> productosDelPlan = jornada.getPlan().getProductos().stream()
                .collect(Collectors.toMap(ProductoVeterinarioEntity::getId, Function.identity()));
        if (!productosDelPlan.keySet().equals(new HashSet<>(productoIdsEntrada))) {
            throw new DatosJornadaInvalidosException(
                    "La ejecución debe aplicar exactamente los productos asociados al plan sanitario.");
        }
        Map<Long, ProductoVeterinarioEntity> productos = productoIdsEntrada.stream()
                .collect(Collectors.toMap(Function.identity(), id -> productoRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "El producto veterinario no existe: " + id))));
        Map<Long, RegistroProductoJornada> dosisPorProducto = detalleProductos.stream()
                .collect(Collectors.toMap(RegistroProductoJornada::productoId, Function.identity()));

        LocalDate hoy = LocalDate.now();
        int intervaloMinimoDias = intervaloMinimo(jornada.getPlan());
        validarIntervaloMinimo(animalesDelLote, jornada.getPlan(), hoy, intervaloMinimoDias);
        List<ConsumoInventario> consumos = new ArrayList<>();
        List<AplicacionSanitariaEntity> aplicaciones = new ArrayList<>();
        for (Long productoId : productoIdsEntrada) {
            ProductoVeterinarioEntity producto = productos.get(productoId);
            RegistroProductoJornada productoEntrada = dosisPorProducto.get(productoId);
            double dosisPorKg = productoEntrada.dosisMlPorKg();
            if (!Double.isFinite(dosisPorKg) || dosisPorKg <= 0) {
                throw new DatosJornadaInvalidosException("La dosis por kilogramo debe ser mayor que cero.");
            }
            if ((producto.getDosisMinima() != null && dosisPorKg < producto.getDosisMinima())
                    || (producto.getDosisMaxima() != null && dosisPorKg > producto.getDosisMaxima())) {
                String dosisMinima = producto.getDosisMinima() == null
                        ? "sin límite inferior" : producto.getDosisMinima().toString();
                String dosisMaxima = producto.getDosisMaxima() == null
                        ? "sin límite superior" : producto.getDosisMaxima().toString();
                throw new DosisFueraDeRangoException(
                        "La dosis de " + producto.getNombre() + " debe estar entre "
                                + dosisMinima + " y " + dosisMaxima + " ml/kg.");
            }
            double dosisTotal = animalesDelLote.stream()
                    .mapToDouble(animal -> animal.getPesoKg() * dosisPorKg)
                    .sum();
            int unidades = costoProductoStrategy.calcularUnidadesRequeridas(
                    dosisTotal, producto.getContenidoMlPorUnidad());
            List<InventarioEntity> inventarios =
                    inventarioRepository.findDisponiblesPorProductoForUpdate(productoId, hoy);
            long disponibles = inventarios.stream().mapToLong(InventarioEntity::getCantidadDisponible).sum();
            if (disponibles < unidades) {
                throw new InventarioInsuficienteException(producto.getNombre(), disponibles, unidades);
            }
            consumos.add(new ConsumoInventario(inventarios, unidades));

            for (AnimalEntity animal : animalesDelLote) {
                double dosisIndividual = animal.getPesoKg() * dosisPorKg;
                if (!Double.isFinite(dosisIndividual) || dosisIndividual <= 0) {
                    throw new DatosJornadaInvalidosException(
                            "La dosis calculada para un animal no es válida.");
                }
                aplicaciones.add(jornadaFactory.crearAplicacion(
                        animal,
                        producto,
                        jornada,
                        hoy,
                        dosisIndividual,
                        intervaloMinimoDias,
                        productoEntrada.observacion()
                ));
            }
        }

        List<JornadaConsumoInventarioEntity> registrosConsumo = consumos.stream()
                .flatMap(consumo -> consumo.descontar(jornada).stream())
                .toList();
        inventarioRepository.saveAllAndFlush(consumos.stream()
                .flatMap(consumo -> consumo.inventarios().stream())
                .toList());
        consumoRepository.saveAllAndFlush(registrosConsumo);
        aplicacionRepository.saveAllAndFlush(aplicaciones);
        jornada.setEstado("EJECUTADA");
        jornadaRepository.save(jornada);

        List<AplicacionJornadaSalida> salida = aplicaciones.stream()
                .map(aplicacion -> new AplicacionJornadaSalida(
                        aplicacion.getAnimal().getId(),
                        aplicacion.getProducto().getId(),
                        aplicacion.getFechaAplicacion(),
                        aplicacion.getDosisAplicada(),
                        aplicacion.getProximaFechaAplicacion(),
                        aplicacion.getObservacion(),
                        aplicacion.getFechaFinRetiroCarne(),
                        aplicacion.getFechaFinRetiroLeche()
                ))
                .toList();
        return new EjecucionJornadaSalida(jornada.getId(), jornada.getEstado(), salida.size(), salida, hoy);
    }

    @Transactional(readOnly = true)
    public List<AplicacionJornadaSalida> consultarAplicacionesPorJornada(Long jornadaId) {
        return consultarAplicacionesPorJornada(jornadaId, null);
    }

    @Transactional(readOnly = true)
    public List<AplicacionJornadaSalida> consultarAplicacionesPorJornada(Long jornadaId, String username) {
        if (jornadaId == null || jornadaId <= 0) {
            throw new DatosJornadaInvalidosException("El identificador de la jornada no es válido.");
        }
        if (username != null) {
            JornadaSanitariaEntity jornada = jornadaRepository.findById(jornadaId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("La jornada"));
            verificarPropiedad(jornada, username);
        }
        return aplicacionRepository.findByJornadaWithDetails(jornadaId).stream()
                .map(aplicacion -> new AplicacionJornadaSalida(
                        aplicacion.getAnimal().getId(),
                        aplicacion.getProducto().getId(),
                        aplicacion.getFechaAplicacion(),
                        aplicacion.getDosisAplicada(),
                        aplicacion.getProximaFechaAplicacion(),
                        aplicacion.getObservacion(),
                        aplicacion.getFechaFinRetiroCarne(),
                        aplicacion.getFechaFinRetiroLeche()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public RegistroJornadaSalida consultarJornada(Long jornadaId, String username) {
        JornadaSanitariaEntity jornada = jornadaRepository.findById(jornadaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La jornada"));
        verificarPropiedad(jornada, username);
        return new RegistroJornadaSalida(jornada.getId(),
                jornada.getLote() == null ? null : jornada.getLote().getId(),
                jornada.getPlan() == null ? null : jornada.getPlan().getId(),
                jornada.getFecha(), jornada.getDescripcion(), jornada.getCantidadAnimales(),
                jornada.getCostoTotal(), jornada.getEstado());
    }

    @Transactional(readOnly = true)
    public Page<RegistroJornadaSalida> listarJornadasPropias(String username, Pageable pageable) {
        if (username == null || username.isBlank()) {
            throw new DatosJornadaInvalidosException(
                    "Se requiere un usuario autenticado para consultar jornadas.");
        }
        return jornadaRepository.findByUsuarioUsername(username, pageable).map(jornada ->
                new RegistroJornadaSalida(jornada.getId(),
                        jornada.getLote() == null ? null : jornada.getLote().getId(),
                        jornada.getPlan() == null ? null : jornada.getPlan().getId(),
                        jornada.getFecha(), jornada.getDescripcion(), jornada.getCantidadAnimales(),
                        jornada.getCostoTotal(), jornada.getEstado()));
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

    private int intervaloMinimo(PlanSanitarioEntity plan) {
        return plan.getIntervaloMinimoDias() == null ? 7 : plan.getIntervaloMinimoDias();
    }

    private void validarIntervaloMinimo(
            List<AnimalEntity> animales, PlanSanitarioEntity plan, LocalDate fecha, int intervaloMinimoDias
    ) {
        if (intervaloMinimoDias < 0) {
            throw new DatosJornadaInvalidosException("El intervalo mínimo del plan no puede ser negativo.");
        }
        LocalDate fechaLimite = fecha.minusDays(intervaloMinimoDias);
        for (AnimalEntity animal : animales) {
            for (ProductoVeterinarioEntity producto : plan.getProductos()) {
                if (aplicacionRepository.existsByAnimalIdAndProductoIdAndFechaAplicacionGreaterThan(
                        animal.getId(), producto.getId(), fechaLimite)) {
                    throw new IntervaloMinimoNoCumplidoException(
                            "El animal " + animal.getId() + " recibió " + producto.getNombre()
                                    + " antes de cumplir el intervalo mínimo de "
                                    + intervaloMinimoDias + " días.");
                }
            }
        }
    }

    private record ConsumoInventario(List<InventarioEntity> inventarios, int unidades) {
        private List<JornadaConsumoInventarioEntity> descontar(JornadaSanitariaEntity jornada) {
            long disponibles = inventarios.stream()
                    .mapToLong(InventarioEntity::getCantidadDisponible).sum();
            if (disponibles < unidades) {
                throw new InventarioInsuficienteException(
                        inventarios.get(0).getProducto().getNombre(), disponibles, unidades);
            }
            int pendientes = unidades;
            List<JornadaConsumoInventarioEntity> registros = new ArrayList<>();
            for (InventarioEntity inventario : inventarios) {
                int usadas = Math.min(pendientes, inventario.getCantidadDisponible());
                if (usadas > 0) {
                    inventario.setCantidadDisponible(inventario.getCantidadDisponible() - usadas);
                    registros.add(new JornadaConsumoInventarioEntity(
                            jornada, inventario, usadas, inventario.getCostoUnitario()));
                }
                pendientes -= usadas;
                if (pendientes == 0) return registros;
            }
            throw new DatosJornadaInvalidosException(
                    "No fue posible distribuir el consumo entre los lotes disponibles.");
        }
    }
}
