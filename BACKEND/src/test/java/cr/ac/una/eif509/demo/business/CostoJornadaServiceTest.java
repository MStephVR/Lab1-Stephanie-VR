package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.ReglaNegocioException;
import cr.ac.una.eif509.demo.data.AplicacionSanitariaRepository;
import cr.ac.una.eif509.demo.data.JornadaConsumoInventarioRepository;
import cr.ac.una.eif509.demo.data.JornadaSanitariaRepository;
import cr.ac.una.eif509.demo.data.UsuarioRepository;
import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.AplicacionSanitariaEntity;
import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.JornadaConsumoInventarioEntity;
import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostoJornadaServiceTest {

    @Mock(name = "costoProductoStrategy") private CostoProductoStrategy costoProductoStrategy;
    @Mock(name = "costoPorMililitroStrategy") private CostoProductoStrategy costoPorMililitroStrategy;
    @Mock private JornadaSanitariaRepository jornadaRepository;
    @Mock private AplicacionSanitariaRepository aplicacionRepository;
    @Mock private JornadaConsumoInventarioRepository consumoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    private CostoJornadaService service;

    @BeforeEach
    void setUp() {
        service = new CostoJornadaService(costoProductoStrategy, costoPorMililitroStrategy,
                jornadaRepository, aplicacionRepository, consumoRepository, usuarioRepository);
    }

    @Test
    void calcularCostoJornada_calculaUnidadesRedondeadasYPromedio() {
        when(costoProductoStrategy.calcularUnidadesRequeridas(150.0, 100.0)).thenReturn(2);
        when(costoPorMililitroStrategy.calcularCostoProducto(new BigDecimal("2500.00"), 150.0, 100.0))
                .thenReturn(new BigDecimal("3750.00"));

        CostoJornada resultado = service.calcularCostoJornada(solicitudValida());

        assertEquals(2, resultado.unidadesRequeridas());
        assertEquals(new BigDecimal("3750.00"), resultado.costoProducto());
        assertEquals(new BigDecimal("4700.00"), resultado.costoTotal());
        assertEquals(new BigDecimal("2350.00"), resultado.costoPromedioPorAnimal());
    }

    @Test
    void calcularCostoJornada_rechazaCantidadNulaEnLugarDeInventarUnAnimal() {
        SolicitudCostoJornada solicitud = new SolicitudCostoJornada(
                null, 150.0, 0.5, 100.0, BigDecimal.TEN,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        assertThrows(ReglaNegocioException.class, () -> service.calcularCostoJornada(solicitud));
        verify(costoProductoStrategy, never()).calcularUnidadesRequeridas(150.0, 100.0);
    }

    @Test
    void calcularCostoJornada_rechazaPesoNoFinito() {
        SolicitudCostoJornada solicitud = new SolicitudCostoJornada(
                2, Double.NaN, 0.5, 100.0, BigDecimal.TEN,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        assertThrows(ReglaNegocioException.class, () -> service.calcularCostoJornada(solicitud));
    }

    @Test
    void calcularCostoJornada_rechazaDosisCero() {
        SolicitudCostoJornada solicitud = new SolicitudCostoJornada(
                2, 150.0, 0.0, 100.0, BigDecimal.TEN,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        assertThrows(ReglaNegocioException.class, () -> service.calcularCostoJornada(solicitud));
    }

    @Test
    void calcularCostoJornada_rechazaCostoAdicionalNegativo() {
        SolicitudCostoJornada solicitud = new SolicitudCostoJornada(
                2, 150.0, 0.5, 100.0, BigDecimal.TEN,
                new BigDecimal("-0.01"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        assertThrows(ReglaNegocioException.class, () -> service.calcularCostoJornada(solicitud));
    }

    @Test
    void procesarCostoJornada_calculaCostoDeLotesConsumidosSinVolverADescontarInventario() {
        JornadaSanitariaEntity jornada = jornada(20L, "EJECUTADA");
        InventarioEntity inventario = inventario(8L, 0);
        JornadaConsumoInventarioEntity consumo = new JornadaConsumoInventarioEntity(
                jornada, inventario, 2, new BigDecimal("20.00"));
        when(jornadaRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(jornada));
        when(aplicacionRepository.findByJornadaWithDetails(20L)).thenReturn(List.of(aplicacion(1L, 150.0)));
        when(consumoRepository.findByJornadaWithInventory(20L)).thenReturn(List.of(consumo));
        when(costoProductoStrategy.calcularCostoProducto(new BigDecimal("20.00"), 2))
                .thenReturn(new BigDecimal("40.00"));

        ProcesamientoCostoSalida salida = service.procesarCostoJornada(entradaCosto());

        assertEquals(new BigDecimal("290.00"), salida.costoTotal());
        assertEquals(0, inventario.getCantidadDisponible());
        assertEquals("COSTEADA", jornada.getEstado());
        assertEquals(new BigDecimal("290.00"), jornada.getCostoTotal());
        verify(jornadaRepository).saveAndFlush(jornada);
        verify(consumoRepository).findByJornadaWithInventory(20L);
    }

    @Test
    void procesarCostoJornada_rechazaJornadaNoEjecutada() {
        when(jornadaRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(jornada(20L, "PLANIFICADA")));
        assertThrows(ReglaNegocioException.class, () -> service.procesarCostoJornada(entradaCosto()));
        verify(consumoRepository, never()).findByJornadaWithInventory(20L);
    }

    @Test
    void procesarCostoJornada_rechazaJornadaSinConsumoRegistrado() {
        when(jornadaRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(jornada(20L, "EJECUTADA")));
        when(aplicacionRepository.findByJornadaWithDetails(20L)).thenReturn(List.of(aplicacion(1L, 150.0)));
        when(consumoRepository.findByJornadaWithInventory(20L)).thenReturn(List.of());

        assertThrows(ReglaNegocioException.class, () -> service.procesarCostoJornada(entradaCosto()));
        verify(jornadaRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void procesarCostoJornada_sumaProductosYCostosDeLotesDiferentes() {
        JornadaSanitariaEntity jornada = jornada(20L, "EJECUTADA");
        ProductoVeterinarioEntity segundoProducto = producto(11L, "30.00");
        InventarioEntity primerLote = inventario(8L, 0);
        InventarioEntity segundoLote = new InventarioEntity(
                segundoProducto, "LOTE-SEGUNDO", LocalDate.now().plusDays(30), 4, new BigDecimal("30.00"));
        List<JornadaConsumoInventarioEntity> consumos = List.of(
                new JornadaConsumoInventarioEntity(jornada, primerLote, 2, new BigDecimal("20.00")),
                new JornadaConsumoInventarioEntity(jornada, segundoLote, 1, new BigDecimal("30.00")));
        when(jornadaRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(jornada));
        when(aplicacionRepository.findByJornadaWithDetails(20L)).thenReturn(List.of(
                aplicacion(1L, 150.0, producto()),
                aplicacion(1L, 75.0, segundoProducto)));
        when(consumoRepository.findByJornadaWithInventory(20L)).thenReturn(consumos);
        when(costoProductoStrategy.calcularCostoProducto(new BigDecimal("20.00"), 2))
                .thenReturn(new BigDecimal("40.00"));
        when(costoProductoStrategy.calcularCostoProducto(new BigDecimal("30.00"), 1))
                .thenReturn(new BigDecimal("30.00"));

        ProcesamientoCostoSalida salida = service.procesarCostoJornada(entradaCosto());

        assertEquals(3, salida.unidadesConsumidas());
        assertEquals(new BigDecimal("70.00"), salida.costoProducto());
        assertEquals(new BigDecimal("320.00"), salida.costoTotal());
    }

    private SolicitudCostoJornada solicitudValida() {
        return new SolicitudCostoJornada(
                2, 150.0, 0.5, 100.0, new BigDecimal("2500.00"),
                new BigDecimal("500.00"), new BigDecimal("150.00"),
                new BigDecimal("200.00"), new BigDecimal("100.00"));
    }

    private ProcesarCostoJornadaEntrada entradaCosto() {
        return new ProcesarCostoJornadaEntrada(
                20L, new BigDecimal("100.00"), new BigDecimal("50.00"),
                new BigDecimal("75.00"), new BigDecimal("25.00"));
    }

    private JornadaSanitariaEntity jornada(Long id, String estado) {
        JornadaSanitariaEntity jornada = new JornadaSanitariaEntity(
                LocalDate.now(), "Campaña", 1, BigDecimal.ZERO, null);
        jornada.setId(id);
        jornada.setEstado(estado);
        return jornada;
    }

    private InventarioEntity inventario(Long id, int cantidad) {
        InventarioEntity inventario = new InventarioEntity(
                producto(), "LOTE-COSTO", LocalDate.now().plusDays(30), cantidad, new BigDecimal("20.00"));
        inventario.setId(id);
        return inventario;
    }

    private AplicacionSanitariaEntity aplicacion(Long animalId, double dosis) {
        return aplicacion(animalId, dosis, producto());
    }

    private AplicacionSanitariaEntity aplicacion(
            Long animalId, double dosis, ProductoVeterinarioEntity producto
    ) {
        AnimalEntity animal = new AnimalEntity(
                "ARETE-" + animalId, "Brahman", "Hembra", 300.0,
                LocalDate.of(2022, 1, 1), "Activo", null);
        animal.setId(animalId);
        return new AplicacionSanitariaEntity(
                animal, producto, null, LocalDate.now(), dosis, LocalDate.now().plusDays(7), null);
    }

    private ProductoVeterinarioEntity producto() {
        return producto(10L, "20.00");
    }

    private ProductoVeterinarioEntity producto(Long id, String costo) {
        ProductoVeterinarioEntity producto = new ProductoVeterinarioEntity(
                "Vacuna " + id, "Vacuna", "ml", 0.1, 1.0, new BigDecimal(costo), 100.0);
        producto.setId(id);
        return producto;
    }
}
