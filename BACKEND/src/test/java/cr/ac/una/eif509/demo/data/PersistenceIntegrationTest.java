package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.CompraEntity;
import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.LoteEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import cr.ac.una.eif509.demo.domain.ProveedorEntity;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@SuppressWarnings({"resource", "null"})
class PersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("ganado_test")
            .withUsername("ganado_user")
            .withPassword("ganado_pass");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
                registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
                registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private ProductoVeterinarioRepository productoRepository;
    @Autowired
    private InventarioRepository inventarioRepository;
    @Autowired
    private ProveedorRepository proveedorRepository;
    @Autowired
    private CompraRepository compraRepository;
        @MockBean
        private BitacoraRepository bitacoraRepository;

    @BeforeEach
    void cleanDatabase() {
        compraRepository.deleteAll();
        inventarioRepository.deleteAll();
        animalRepository.deleteAll();
        productoRepository.deleteAll();
        proveedorRepository.deleteAll();
        loteRepository.deleteAll();
    }

    @Test
    void persisteAnimalYConsultaPorLote() {
        LoteEntity lote = loteRepository.save(new LoteEntity("Lote Integracion", "Finca Norte", "Cria"));
        animalRepository.save(new AnimalEntity("TC-001", "Brahman", "Hembra", 420.0,
                LocalDate.of(2023, 1, 10), "Activo", lote));

        List<AnimalEntity> animales = animalRepository.findByLoteId(lote.getId());

        assertThat(animales).hasSize(1);
        assertThat(animales.get(0).getCodigoArete()).isEqualTo("TC-001");
    }

    @Test
    void consultaAnimalesConLoteEnUnaConsultaJoinFetch() {
        LoteEntity lote = loteRepository.save(new LoteEntity("Lote Fetch", "Finca Sur", "Leche"));
        animalRepository.save(new AnimalEntity("TC-002", "Holstein", "Macho", 500.0,
                LocalDate.of(2022, 5, 20), "Activo", lote));

        List<AnimalEntity> animales = animalRepository.findAllWithLote();

        assertThat(animales).singleElement().extracting(animal -> animal.getLote().getNombre())
                .isEqualTo("Lote Fetch");
    }

    @Test
    void consultaInventarioDisponibleYNoVencido() {
        ProductoVeterinarioEntity producto = productoRepository.save(new ProductoVeterinarioEntity(
                "Vacuna TC", "Vacuna", "ml", 1.0, 5.0, BigDecimal.valueOf(12.50)));
        inventarioRepository.save(new InventarioEntity(producto, "LOTE-TC", LocalDate.now().plusDays(30),
                10, BigDecimal.valueOf(8.00)));

        List<InventarioEntity> inventario = inventarioRepository.findDisponibleNoVencido(LocalDate.now());

        assertThat(inventario).singleElement().extracting(item -> item.getProducto().getNombre())
                .isEqualTo("Vacuna TC");
    }

    @Test
    void consultaComprasPorRangoConProveedor() {
        ProveedorEntity proveedor = proveedorRepository.save(new ProveedorEntity(
                "Proveedor TC", "8888-0000", "tc@example.com"));
        compraRepository.save(new CompraEntity(proveedor, LocalDate.of(2026, 9, 1), BigDecimal.valueOf(100)));

        List<CompraEntity> compras = compraRepository.findByFechaBetweenWithProveedor(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertThat(compras).singleElement().extracting(compra -> compra.getProveedor().getNombre())
                .isEqualTo("Proveedor TC");
    }

    @Test
    void filtraAnimalesActivosConSpecificationCriteria() {
        LoteEntity lote = loteRepository.save(new LoteEntity("Lote Criteria", "Finca Este", "Cria"));
        animalRepository.save(new AnimalEntity("TC-003", "Brahman", "Hembra", 410.0,
                LocalDate.of(2023, 3, 2), "Activo", lote));
        animalRepository.save(new AnimalEntity("TC-004", "Brahman", "Macho", 450.0,
                LocalDate.of(2023, 4, 2), "Inactivo", lote));

        List<AnimalEntity> activos = animalRepository.findAll(BusinessSpecifications.animalesActivosDeRaza("brahman"));

        assertThat(activos.stream().map(AnimalEntity::getCodigoArete).toList())
                .containsExactly("TC-003");
    }

    @Test
    void filtraProductosPorRangoDePrecioConSpecificationCriteria() {
        productoRepository.save(new ProductoVeterinarioEntity("Producto Economico", "Medicamento", "ml",
                1.0, 2.0, BigDecimal.valueOf(10)));
        productoRepository.save(new ProductoVeterinarioEntity("Producto Caro", "Medicamento", "ml",
                1.0, 2.0, BigDecimal.valueOf(100)));

        List<ProductoVeterinarioEntity> productos = productoRepository.findAll(
                BusinessSpecifications.productosDentroDelRangoDePrecio(5.0, 20.0));

        assertThat(productos.stream().map(ProductoVeterinarioEntity::getNombre).toList())
                .containsExactly("Producto Economico");
    }
}
