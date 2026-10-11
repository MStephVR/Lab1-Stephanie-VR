package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.business.exception.ReglaNegocioException;
import cr.ac.una.eif509.demo.data.AplicacionSanitariaRepository;
import cr.ac.una.eif509.demo.data.InventarioRepository;
import cr.ac.una.eif509.demo.data.JornadaSanitariaRepository;
import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.AplicacionSanitariaEntity;
import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @Mock private CostoProductoStrategy costoProductoStrategy;
    @Mock private JornadaSanitariaRepository jornadaRepository;
    @Mock private AplicacionSanitariaRepository aplicacionRepository;
    @Mock private InventarioRepository inventarioRepository;
    @InjectMocks private CostoJornadaService service;

    @Test
    void calcularCostoJornada_calculaUnidadesRedondeadasYPromedio() {
        when(costoProductoStrategy.calcularUnidadesRequeridas(150.0, 100.0)).thenReturn(2);
        when(costoProductoStrategy.calcularCostoProducto(new BigDecimal("2500.00"), 2))
                .thenReturn(new BigDecimal("5000.00"));

        CostoJornada resultado = service.calcularCostoJornada(solicitudValida());

        assertEquals(2, resultado.unidadesRequeridas());
        assertEquals(new BigDecimal("5000.00"), resultado.costoProducto());
        assertEquals(new BigDecimal("5950.00"), resultado.costoTotal());
        assertEquals(new BigDecimal("2975.00"), resultado.costoPromedioPorAnimal());
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
    void procesarCostoJornada_calculaCostosYDecrementaInventario() {
        JornadaSanitariaEntity jornada = jornada(20L, "EJECUTADA");
        InventarioEntity inventario = inventario(8L, 10);
        when(jornadaRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(jornada));
        when(aplicacionRepository.findByJornadaWithDetails(20L)).thenReturn(List.of(aplicacion(1L, 150.0)));
        when(inventarioRepository.findDisponiblesPorProductoForUpdate(10L, LocalDate.now()))
                .thenReturn(List.of(inventario));
        when(costoProductoStrategy.calcularUnidadesRequeridas(150.0, 100.0)).thenReturn(2);
        when(costoProductoStrategy.calcularCostoProducto(new BigDecimal("20.00"), 2))
                .thenReturn(new BigDecimal("40.00"));

        ProcesamientoCostoSalida salida = service.procesarCostoJornada(entradaCosto());

        assertEquals(new BigDecimal("290.00"), salida.costoTotal());
        assertEquals(8, inventario.getCantidadDisponible());
        assertEquals("COSTEADA", jornada.getEstado());
        assertEquals(new BigDecimal("290.00"), jornada.getCostoTotal());
        verify(jornadaRepository).saveAndFlush(jornada);
        verify(inventarioRepository).saveAll(List.of(inventario));
    }

    @Test
    void procesarCostoJornada_rechazaJornadaNoEjecutada() {
        when(jornadaRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(jornada(20L, "PLANIFICADA")));
        assertThrows(ReglaNegocioException.class, () -> service.procesarCostoJornada(entradaCosto()));
        verify(inventarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void procesarCostoJornada_rechazaInventarioInsuficiente() {
        when(jornadaRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(jornada(20L, "EJECUTADA")));
        when(aplicacionRepository.findByJornadaWithDetails(20L)).thenReturn(List.of(aplicacion(1L, 150.0)));
        when(inventarioRepository.findDisponiblesPorProductoForUpdate(10L, LocalDate.now()))
                .thenReturn(List.of(inventario(8L, 1)));
        when(costoProductoStrategy.calcularUnidadesRequeridas(150.0, 100.0)).thenReturn(2);

        assertThrows(ReglaNegocioException.class, () -> service.procesarCostoJornada(entradaCosto()));
        verify(jornadaRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
        verify(inventarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
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
        AnimalEntity animal = new AnimalEntity(
                "ARETE-" + animalId, "Brahman", "Hembra", 300.0,
                LocalDate.of(2022, 1, 1), "Activo", null);
        animal.setId(animalId);
        return new AplicacionSanitariaEntity(
                animal, producto(), null, LocalDate.now(), dosis, LocalDate.now().plusDays(7), null);
    }

    private ProductoVeterinarioEntity producto() {
        ProductoVeterinarioEntity producto = new ProductoVeterinarioEntity(
                "Vacuna", "Vacuna", "ml", 0.1, 1.0, new BigDecimal("20.00"), 100.0);
        producto.setId(10L);
        return producto;
    }
}
