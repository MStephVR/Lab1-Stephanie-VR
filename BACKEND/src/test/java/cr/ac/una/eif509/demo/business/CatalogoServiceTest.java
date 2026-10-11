package cr.ac.una.eif509.demo.business;

import cr.ac.una.eif509.demo.data.AnimalRepository;
import cr.ac.una.eif509.demo.data.CompraRepository;
import cr.ac.una.eif509.demo.data.InventarioRepository;
import cr.ac.una.eif509.demo.data.LoteRepository;
import cr.ac.una.eif509.demo.data.PlanSanitarioRepository;
import cr.ac.una.eif509.demo.data.ProveedorRepository;
import cr.ac.una.eif509.demo.data.UsuarioRepository;
import cr.ac.una.eif509.demo.data.ProductoVeterinarioRepository;
import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.CompraEntity;
import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.LoteEntity;
import cr.ac.una.eif509.demo.domain.PlanSanitarioEntity;
import cr.ac.una.eif509.demo.domain.ProveedorEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {
    @Mock private AnimalRepository animalRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private ProductoVeterinarioRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private PlanSanitarioRepository planRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private CompraRepository compraRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private CatalogoService service;

    @BeforeEach
    void setUp() {
        service = new CatalogoService(animalRepository, loteRepository, productoRepository,
                inventarioRepository, planRepository, proveedorRepository, compraRepository, usuarioRepository);
    }

    @Test
    void listaYConsultaAnimalesComoDtos() {
        AnimalEntity animal = animal();
        when(animalRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(animal)));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        var pagina = service.animales("Activo", "Brahman", PageRequest.of(0, 10));
        AnimalSalida salida = service.animal(1L);

        assertEquals(1L, pagina.getTotalElements());
        assertEquals("BRAH-1", pagina.getContent().get(0).codigoArete());
        assertEquals(1L, salida.loteId());
    }

    @Test
    void creaActualizaYEliminaAnimal() {
        LoteEntity lote = lote();
        AnimalEntity existente = animal();
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(animalRepository.save(any(AnimalEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(animalRepository.existsById(1L)).thenReturn(true);

        AnimalEntrada entrada = new AnimalEntrada("BRAH-2", "Brahman", "Hembra", 315.0,
                LocalDate.of(2023, 1, 1), "Activo", 1L);
        assertEquals("BRAH-2", service.crearAnimal(entrada).codigoArete());
        assertEquals("BRAH-2", service.actualizarAnimal(1L, entrada).codigoArete());
        service.eliminarAnimal(1L);
        verify(animalRepository).deleteById(1L);
    }

    @Test
    void gestionaLotesYProductosConSusDtos() {
        LoteEntity lote = lote();
        ProductoVeterinarioEntity producto = producto();
        when(loteRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(lote)));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(LoteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loteRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(producto)));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(ProductoVeterinarioEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productoRepository.existsById(1L)).thenReturn(true);

        LoteEntrada loteEntrada = new LoteEntrada("Lote 2", "Finca", "Cría", true);
        assertEquals(1L, service.lotes(PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1L, service.lote(1L).id());
        assertEquals("Lote 2", service.crearLote(loteEntrada).nombre());
        assertEquals("Lote 2", service.actualizarLote(1L, loteEntrada).nombre());
        service.eliminarLote(1L);
        verify(loteRepository).deleteById(1L);

        ProductoEntrada productoEntrada = new ProductoEntrada("Vacuna 2", "Vacuna", "ml",
                0.1, 1.0, new BigDecimal("25.00"), 100.0);
        assertEquals(1L, service.productos(10.0, 100.0, PageRequest.of(0, 10)).getTotalElements());
        assertEquals("Vacuna", service.producto(1L).tipo());
        assertEquals("Vacuna 2", service.crearProducto(productoEntrada).nombre());
        assertEquals("Vacuna 2", service.actualizarProducto(1L, productoEntrada).nombre());
        service.eliminarProducto(1L);
        verify(productoRepository).deleteById(1L);
    }

    @Test
    void gestionaInventariosConSuProducto() {
        InventarioEntity inventario = inventario();
        when(inventarioRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(inventario)));
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(InventarioEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(inventarioRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto()));

        InventarioEntrada entrada = new InventarioEntrada(1L, "LOTE-2", LocalDate.now().plusDays(10),
                8, new BigDecimal("25.00"));
        assertEquals(1L, service.inventarios(PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1L, service.inventario(1L).productoId());
        assertEquals("LOTE-2", service.crearInventario(entrada).loteFabricacion());
        assertEquals("LOTE-2", service.actualizarInventario(1L, entrada).loteFabricacion());
        service.eliminarInventario(1L);
        verify(inventarioRepository).deleteById(1L);
    }

    @Test
    void gestionaPlanesYResuelveReferenciasAProductos() {
        PlanSanitarioEntity plan = new PlanSanitarioEntity("Plan 1", "Descripción", 12);
        plan.setId(1L);
        plan.getProductos().add(producto());
        when(planRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(plan)));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(PlanSanitarioEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(planRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.findAllById(List.of(1L))).thenReturn(List.of(producto()));

        PlanSanitarioEntrada entrada = new PlanSanitarioEntrada("Plan 2", "Descripción 2", 18, List.of(1L));
        assertEquals(1L, service.planes(PageRequest.of(0, 10)).getTotalElements());
        assertEquals(List.of(1L), service.plan(1L).productosIds());
        assertEquals("Plan 2", service.crearPlan(entrada).nombre());
        assertEquals("Plan 2", service.actualizarPlan(1L, entrada).nombre());
        service.eliminarPlan(1L);
        verify(planRepository).deleteById(1L);
    }

    @Test
    void gestionaProveedoresYComprasConDtos() {
        ProveedorEntity proveedor = new ProveedorEntity("Proveedor", "8888", "proveedor@example.test");
        proveedor.setId(1L);
        CompraEntity compra = new CompraEntity(proveedor, LocalDate.now(), new BigDecimal("100.00"));
        compra.setId(1L);
        when(proveedorRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(proveedor)));
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.save(any(ProveedorEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(proveedorRepository.existsById(1L)).thenReturn(true);
        when(compraRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(compra)));
        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(compraRepository.save(any(CompraEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(compraRepository.existsById(1L)).thenReturn(true);

        ProveedorEntrada proveedorEntrada = new ProveedorEntrada("Proveedor 2", "7777", "nuevo@example.test");
        assertEquals(1L, service.proveedores(PageRequest.of(0, 10)).getTotalElements());
        assertEquals("Proveedor", service.proveedor(1L).nombre());
        assertEquals("Proveedor 2", service.crearProveedor(proveedorEntrada).nombre());
        assertEquals("Proveedor 2", service.actualizarProveedor(1L, proveedorEntrada).nombre());
        service.eliminarProveedor(1L);
        verify(proveedorRepository).deleteById(1L);

        CompraEntrada compraEntrada = new CompraEntrada(1L, LocalDate.now(), new BigDecimal("250.00"));
        assertEquals(1L, service.compras(PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1L, service.compra(1L).proveedorId());
        assertEquals(new BigDecimal("250.00"), service.crearCompra(compraEntrada).total());
        assertEquals(new BigDecimal("250.00"), service.actualizarCompra(1L, compraEntrada).total());
        service.eliminarCompra(1L);
        verify(compraRepository).deleteById(1L);
    }

    private AnimalEntity animal() {
        AnimalEntity animal = new AnimalEntity("BRAH-1", "Brahman", "Hembra", 300.0,
                LocalDate.of(2022, 1, 1), "Activo", lote());
        animal.setId(1L);
        return animal;
    }

    private LoteEntity lote() {
        LoteEntity lote = new LoteEntity("Lote 1", "Finca", "Cría");
        lote.setId(1L);
        return lote;
    }

    private ProductoVeterinarioEntity producto() {
        ProductoVeterinarioEntity producto = new ProductoVeterinarioEntity("Vacuna", "Vacuna", "ml",
                0.1, 1.0, new BigDecimal("20.00"), 100.0);
        producto.setId(1L);
        return producto;
    }

    private InventarioEntity inventario() {
        InventarioEntity inventario = new InventarioEntity(producto(), "LOTE-1",
                LocalDate.now().plusDays(20), 10, new BigDecimal("20.00"));
        inventario.setId(1L);
        return inventario;
    }
}
