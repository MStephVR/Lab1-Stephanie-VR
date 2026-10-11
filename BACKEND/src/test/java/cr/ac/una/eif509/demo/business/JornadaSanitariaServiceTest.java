package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.ReglaNegocioException;
import cr.ac.una.eif509.demo.data.AnimalRepository;
import cr.ac.una.eif509.demo.data.AplicacionSanitariaRepository;
import cr.ac.una.eif509.demo.data.InventarioRepository;
import cr.ac.una.eif509.demo.data.JornadaSanitariaRepository;
import cr.ac.una.eif509.demo.data.LoteRepository;
import cr.ac.una.eif509.demo.data.PlanSanitarioRepository;
import cr.ac.una.eif509.demo.data.ProductoVeterinarioRepository;
import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.AplicacionSanitariaEntity;
import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import cr.ac.una.eif509.demo.domain.LoteEntity;
import cr.ac.una.eif509.demo.domain.PlanSanitarioEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JornadaSanitariaServiceTest {

    @Mock private JornadaSanitariaRepository jornadaRepository;
    @Mock private AplicacionSanitariaRepository aplicacionRepository;
    @Mock private ProductoVeterinarioRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private PlanSanitarioRepository planRepository;
    @Mock private AnimalRepository animalRepository;
    @Mock private CostoProductoStrategy costoProductoStrategy;
    @Spy private JornadaSanitariaFactory jornadaFactory = new JornadaSanitariaFactory();
    @InjectMocks private JornadaSanitariaService service;

    @Test
    void planificar_creaJornadaPlanificadaConCantidadDeAnimalesDelLote() {
        stubPlanificacion(2);
        when(jornadaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroJornadaSalida salida = service.planificar(planificarEntrada());

        assertEquals(1L, salida.loteId());
        assertEquals(2, salida.cantidadAnimales());
        assertEquals("Jornada planificada", salida.mensaje());
        ArgumentCaptor<JornadaSanitariaEntity> captor = ArgumentCaptor.forClass(JornadaSanitariaEntity.class);
        verify(jornadaRepository).save(captor.capture());
        assertEquals("PLANIFICADA", captor.getValue().getEstado());
    }

    @Test
    void planificar_rechazaLoteInactivo() {
        LoteEntity lote = lote(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));

        assertThrows(ReglaNegocioException.class, () -> service.planificar(planificarEntrada()));
        verify(planRepository, never()).findById(10L);
        verify(jornadaRepository, never()).save(any());
    }

    @Test
    void planificar_rechazaLoteSinAnimalesActivos() {
        stubBasePlanificacion();
        when(animalRepository.findByLoteId(1L)).thenReturn(List.of(animal(1L, 100.0, "Inactivo")));

        assertThrows(ReglaNegocioException.class, () -> service.planificar(planificarEntrada()));
        verify(jornadaRepository, never()).save(any());
    }

    @Test
    void planificar_rechazaPlanSinProductos() {
        stubLote();
        PlanSanitarioEntity plan = new PlanSanitarioEntity("Plan", "Plan vacío", 6);
        plan.setId(10L);
        when(planRepository.findById(10L)).thenReturn(Optional.of(plan));

        assertThrows(ReglaNegocioException.class, () -> service.planificar(planificarEntrada()));
        verify(animalRepository, never()).findByLoteId(1L);
    }

    @Test
    void planificar_rechazaFechaPasada() {
        PlanificarJornadaEntrada entrada = new PlanificarJornadaEntrada(
                1L, 10L, LocalDate.now().minusDays(1), "Vacunación");

        assertThrows(ReglaNegocioException.class, () -> service.planificar(entrada));
        verify(loteRepository, never()).findById(1L);
    }

    @Test
    void ejecutar_registraAplicacionesDescuentaInventarioYActualizaEstado() {
        JornadaSanitariaEntity jornada = jornada("PLANIFICADA", LocalDate.now());
        InventarioEntity inventario = inventario(10, 100.0);
        when(jornadaRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(jornada));
        when(animalRepository.findByLoteId(1L)).thenReturn(List.of(animal(1L, 300.0, "Activo")));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto()));
        when(costoProductoStrategy.calcularUnidadesRequeridas(150.0, 100.0)).thenReturn(2);
        when(inventarioRepository.findDisponiblesPorProductoForUpdate(10L, LocalDate.now()))
                .thenReturn(List.of(inventario));

        EjecucionJornadaSalida salida = service.ejecutar(20L, ejecucion(List.of(1L), 10L, 0.5));

        assertEquals("EJECUTADA", salida.estado());
        assertEquals(1, salida.animalesTratados());
        assertEquals(150.0, salida.aplicaciones().get(0).dosisAplicada());
        assertEquals(8, inventario.getCantidadDisponible());
        verify(inventarioRepository).saveAllAndFlush(List.of(inventario));
        verify(aplicacionRepository).saveAllAndFlush(any());
        verify(jornadaRepository).save(jornada);
    }

    @Test
    void ejecutar_rechazaJornadaEnEstadoIncorrecto() {
        when(jornadaRepository.findByIdForUpdate(20L))
                .thenReturn(Optional.of(jornada("EJECUTADA", LocalDate.now())));

        assertThrows(ReglaNegocioException.class,
                () -> service.ejecutar(20L, ejecucion(List.of(1L), 10L, 0.5)));
        verify(inventarioRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void ejecutar_rechazaCoberturaParcialDelLote() {
        when(jornadaRepository.findByIdForUpdate(20L))
                .thenReturn(Optional.of(jornada("PLANIFICADA", LocalDate.now())));
        when(animalRepository.findByLoteId(1L))
                .thenReturn(List.of(animal(1L, 300.0, "Activo"), animal(2L, 250.0, "Activo")));

        assertThrows(ReglaNegocioException.class,
                () -> service.ejecutar(20L, ejecucion(List.of(1L), 10L, 0.5)));
        verify(inventarioRepository, never()).saveAllAndFlush(any());
        verify(aplicacionRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void ejecutar_rechazaInventarioInsuficienteSinEscrituras() {
        when(jornadaRepository.findByIdForUpdate(20L))
                .thenReturn(Optional.of(jornada("PLANIFICADA", LocalDate.now())));
        when(animalRepository.findByLoteId(1L)).thenReturn(List.of(animal(1L, 300.0, "Activo")));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto()));
        when(costoProductoStrategy.calcularUnidadesRequeridas(150.0, 100.0)).thenReturn(2);
        when(inventarioRepository.findDisponiblesPorProductoForUpdate(10L, LocalDate.now()))
                .thenReturn(List.of(inventario(1, 100.0)));

        assertThrows(ReglaNegocioException.class,
                () -> service.ejecutar(20L, ejecucion(List.of(1L), 10L, 0.5)));
        verify(jornadaRepository, never()).save(any());
        verify(aplicacionRepository, never()).saveAllAndFlush(any());
    }

    @Test
    void ejecutar_rechazaEjecucionAntesDeFechaPlanificada() {
        when(jornadaRepository.findByIdForUpdate(20L))
                .thenReturn(Optional.of(jornada("PLANIFICADA", LocalDate.now().plusDays(1))));

        assertThrows(ReglaNegocioException.class,
                () -> service.ejecutar(20L, ejecucion(List.of(1L), 10L, 0.5)));
        verify(animalRepository, never()).findByLoteId(1L);
    }

    @Test
    void consultarAplicaciones_devuelveDtosSinEntidadesJpa() {
        when(aplicacionRepository.findByJornadaWithDetails(20L))
                .thenReturn(List.of(new AplicacionSanitariaEntity(
                        animal(1L, 300.0, "Activo"), producto(), null, LocalDate.now(),
                        150.0, LocalDate.now().plusDays(7), "observación")));

        List<AplicacionJornadaSalida> salida = service.consultarAplicacionesPorJornada(20L);

        assertEquals(1L, salida.get(0).animalId());
        assertEquals(10L, salida.get(0).productoId());
    }

    private void stubPlanificacion(int cantidadAnimales) {
        stubBasePlanificacion();
        when(animalRepository.findByLoteId(1L)).thenReturn(
                java.util.stream.LongStream.rangeClosed(1, cantidadAnimales)
                        .mapToObj(id -> animal(id, 300.0, "Activo"))
                        .toList());
        when(jornadaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stubBasePlanificacion() {
        stubLote();
        when(planRepository.findById(10L)).thenReturn(Optional.of(planConProductos()));
    }

    private void stubLote() {
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote(true)));
    }

    private LoteEntity lote(boolean activo) {
        LoteEntity lote = new LoteEntity("Lote A", "Finca", "Sanidad");
        lote.setId(1L);
        lote.setActivo(activo);
        return lote;
    }

    private PlanSanitarioEntity planConProductos() {
        PlanSanitarioEntity plan = new PlanSanitarioEntity("Plan", "Vacunación", 6);
        plan.setId(10L);
        plan.getProductos().add(producto());
        return plan;
    }

    private ProductoVeterinarioEntity producto() {
        ProductoVeterinarioEntity producto = new ProductoVeterinarioEntity(
                "Vacuna", "Biológico", "ml", 0.1, 1.0, new BigDecimal("20.00"), 100.0);
        producto.setId(10L);
        return producto;
    }

    private AnimalEntity animal(Long id, Double peso, String estado) {
        AnimalEntity animal = new AnimalEntity("ARETE-" + id, "Brahman", "Hembra", peso,
                LocalDate.of(2023, 1, 1), estado, lote(true));
        animal.setId(id);
        return animal;
    }

    private JornadaSanitariaEntity jornada(String estado, LocalDate fecha) {
        JornadaSanitariaEntity jornada = new JornadaSanitariaEntity(
                fecha, "Vacunación", 1, BigDecimal.ZERO, null, lote(true), planConProductos());
        jornada.setId(20L);
        jornada.setEstado(estado);
        return jornada;
    }

    private InventarioEntity inventario(int cantidad, double costo) {
        return new InventarioEntity(producto(), "LOTE-FEFO", LocalDate.now().plusDays(30),
                cantidad, BigDecimal.valueOf(costo));
    }

    private PlanificarJornadaEntrada planificarEntrada() {
        return new PlanificarJornadaEntrada(1L, 10L, LocalDate.now(), "Vacunación");
    }

    private EjecutarJornadaEntrada ejecucion(List<Long> animales, Long productoId, double dosis) {
        return new EjecutarJornadaEntrada(animales, List.of(new RegistroProductoJornada(productoId, dosis, null)));
    }
}
