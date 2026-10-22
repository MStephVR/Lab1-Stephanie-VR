package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.AplicacionSanitariaEntity;
import cr.ac.una.eif509.demo.domain.BitacoraEvento;
import cr.ac.una.eif509.demo.domain.CompraEntity;
import cr.ac.una.eif509.demo.domain.InventarioEntity;
import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import cr.ac.una.eif509.demo.domain.JornadaConsumoInventarioEntity;
import cr.ac.una.eif509.demo.domain.LoteEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import cr.ac.una.eif509.demo.domain.PlanSanitarioEntity;
import cr.ac.una.eif509.demo.domain.ProveedorEntity;
import cr.ac.una.eif509.demo.business.CostoJornadaService;
import cr.ac.una.eif509.demo.business.EjecutarJornadaEntrada;
import cr.ac.una.eif509.demo.business.JornadaSanitariaService;
import cr.ac.una.eif509.demo.business.ProcesarCostoJornadaEntrada;
import cr.ac.una.eif509.demo.business.RegistroProductoJornada;
import cr.ac.una.eif509.demo.data.AplicacionSanitariaRepository;
import cr.ac.una.eif509.demo.data.JornadaSanitariaRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
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
    @Autowired
    private JornadaSanitariaRepository jornadaRepository;
    @Autowired
    private JornadaConsumoInventarioRepository consumoRepository;
    @Autowired
    private PlanSanitarioRepository planRepository;
    @Autowired
    private AplicacionSanitariaRepository aplicacionRepository;
    @Autowired
    private CostoJornadaService costoJornadaService;
    @Autowired
    private JornadaSanitariaService jornadaSanitariaService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
        @MockBean
        private BitacoraRepository bitacoraRepository;

    @BeforeEach
    void cleanDatabase() {
        aplicacionRepository.deleteAll();
        consumoRepository.deleteAll();
        jornadaRepository.deleteAll();
        planRepository.deleteAll();
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
    void apiFiltraAnimalesActivosPorLote() throws Exception {
        LoteEntity loteSeleccionado = loteRepository.save(
                new LoteEntity("Lote SPA", "Finca", "Cria"));
        LoteEntity otroLote = loteRepository.save(new LoteEntity("Otro lote", "Finca", "Cria"));
        animalRepository.save(new AnimalEntity("SPA-001", "Brahman", "Hembra", 420.0,
                LocalDate.of(2023, 1, 10), "Activo", loteSeleccionado));
        animalRepository.save(new AnimalEntity("SPA-002", "Brahman", "Hembra", 420.0,
                LocalDate.of(2023, 1, 10), "Activo", otroLote));
        animalRepository.save(new AnimalEntity("SPA-003", "Brahman", "Hembra", 420.0,
                LocalDate.of(2023, 1, 10), "Inactivo", loteSeleccionado));

        mockMvc.perform(get("/api/v1/animales")
                        .param("estado", "Activo")
                        .param("loteId", loteSeleccionado.getId().toString())
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + token("admin", "AdminLocal2026!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].codigoArete").value("SPA-001"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void corsAceptaElOrigenLocalDeLaSpa() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("POST")));
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

    @Test
    void revierteCostoYEstadoSiFallaElRegistroDelCosto() {
        LoteEntity lote = loteRepository.save(new LoteEntity("Lote rollback", "Finca", "Sanidad"));
        AnimalEntity animal = animalRepository.save(new AnimalEntity(
                "ROLLBACK-01", "Brahman", "Hembra", 300.0,
                LocalDate.of(2023, 1, 1), "Activo", lote));
        ProductoVeterinarioEntity producto = productoRepository.save(new ProductoVeterinarioEntity(
                "Producto rollback", "Vacuna", "ml", 0.1, 1.0, new BigDecimal("20.00"), 100.0));
        InventarioEntity inventario = inventarioRepository.save(new InventarioEntity(
                producto, "LOTE-ROLLBACK", LocalDate.now().plusDays(30), 8, new BigDecimal("20.00")));
        JornadaSanitariaEntity jornada = new JornadaSanitariaEntity(
                LocalDate.now(), "Jornada rollback", 1, BigDecimal.ZERO, null);
        jornada.setEstado("EJECUTADA");
        jornada = jornadaRepository.save(jornada);
        aplicacionRepository.save(new AplicacionSanitariaEntity(
                animal, producto, jornada, LocalDate.now(), 150.0, LocalDate.now().plusDays(7), null));
        consumoRepository.save(new JornadaConsumoInventarioEntity(
                jornada, inventario, 2, new BigDecimal("20.00")));

        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION fail_journey_cost_update() RETURNS trigger AS $$
                BEGIN
                    RAISE EXCEPTION 'fallo intermedio de prueba';
                END;
                $$ LANGUAGE plpgsql
                """);
        jdbcTemplate.execute("""
                CREATE TRIGGER fail_journey_cost_update
                BEFORE UPDATE ON jornadas_sanitarias
                FOR EACH ROW EXECUTE FUNCTION fail_journey_cost_update()
                """);
        try {
            Long jornadaId = jornada.getId();
            assertThatThrownBy(() -> costoJornadaService.procesarCostoJornada(
                    new ProcesarCostoJornadaEntrada(
                            jornadaId,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)))
                    .isInstanceOf(RuntimeException.class)
                    .hasStackTraceContaining("fallo intermedio de prueba");

            JornadaSanitariaEntity jornadaPersistida = jornadaRepository.findById(jornadaId).orElseThrow();
            InventarioEntity inventarioPersistido = inventarioRepository.findById(inventario.getId()).orElseThrow();
            assertThat(jornadaPersistida.getCostoTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(jornadaPersistida.getEstado()).isEqualTo("EJECUTADA");
            assertThat(inventarioPersistido.getCantidadDisponible()).isEqualTo(8);
        } finally {
            jdbcTemplate.execute("DROP TRIGGER IF EXISTS fail_journey_cost_update ON jornadas_sanitarias");
            jdbcTemplate.execute("DROP FUNCTION IF EXISTS fail_journey_cost_update()");
        }
    }

    @Test
    void revierteEjecucionCompletaSiFallaElRegistroDeAplicacion() {
        LoteEntity lote = loteRepository.save(new LoteEntity("Lote ejecución rollback", "Finca", "Sanidad"));
        AnimalEntity animal = animalRepository.save(new AnimalEntity(
                "EJEC-ROLLBACK-01", "Brahman", "Hembra", 300.0,
                LocalDate.of(2023, 1, 1), "Activo", lote));
        ProductoVeterinarioEntity producto = productoRepository.save(new ProductoVeterinarioEntity(
                "Vacuna ejecución rollback", "Vacuna", "ml", 0.1, 1.0,
                new BigDecimal("20.00"), 100.0));
        PlanSanitarioEntity plan = new PlanSanitarioEntity("Plan ejecución rollback", "Plan de prueba", 6);
        plan.getProductos().add(producto);
        plan = planRepository.save(plan);
        InventarioEntity inventario = inventarioRepository.save(new InventarioEntity(
                producto, "LOTE-EJEC-ROLLBACK", LocalDate.now().plusDays(30), 10, new BigDecimal("20.00")));
        JornadaSanitariaEntity jornada = new JornadaSanitariaEntity(
                LocalDate.now(), "Jornada ejecución rollback", 1, BigDecimal.ZERO, null, lote, plan);
        jornada = jornadaRepository.save(jornada);

        jdbcTemplate.execute("""
                CREATE OR REPLACE FUNCTION fail_application_insert() RETURNS trigger AS $$
                BEGIN
                    RAISE EXCEPTION 'fallo intermedio al registrar aplicación';
                END;
                $$ LANGUAGE plpgsql
                """);
        jdbcTemplate.execute("""
                CREATE TRIGGER fail_application_insert
                BEFORE INSERT ON aplicaciones_sanitarias
                FOR EACH ROW EXECUTE FUNCTION fail_application_insert()
                """);
        try {
            Long jornadaId = jornada.getId();
            Long productoId = producto.getId();
            assertThatThrownBy(() -> jornadaSanitariaService.ejecutar(
                    jornadaId,
                    new EjecutarJornadaEntrada(
                            List.of(animal.getId()),
                            List.of(new RegistroProductoJornada(productoId, 0.5, "Aplicación test")))))
                    .isInstanceOf(RuntimeException.class)
                    .hasStackTraceContaining("fallo intermedio al registrar aplicación");

            JornadaSanitariaEntity persistida = jornadaRepository.findById(jornadaId).orElseThrow();
            InventarioEntity inventarioPersistido =
                    inventarioRepository.findById(inventario.getId()).orElseThrow();
            assertThat(persistida.getEstado()).isEqualTo("PLANIFICADA");
            assertThat(inventarioPersistido.getCantidadDisponible()).isEqualTo(10);
            assertThat(aplicacionRepository.findByJornadaWithDetails(jornadaId)).isEmpty();
        } finally {
            jdbcTemplate.execute("DROP TRIGGER IF EXISTS fail_application_insert ON aplicaciones_sanitarias");
            jdbcTemplate.execute("DROP FUNCTION IF EXISTS fail_application_insert()");
        }

    }

    @Test
    void costeaVariosProductosConLosLotesConsumidosSinUnSegundoDescuento() {
        LoteEntity lote = loteRepository.save(new LoteEntity("Lote costo sin doble descuento", "Finca", "Sanidad"));
        AnimalEntity animal = animalRepository.save(new AnimalEntity(
                "COSTO-REAL-01", "Brahman", "Hembra", 300.0,
                LocalDate.of(2023, 1, 1), "Activo", lote));
        ProductoVeterinarioEntity productoA = productoRepository.save(new ProductoVeterinarioEntity(
                "Vacuna costo real", "Vacuna", "ml", 0.1, 1.0,
                new BigDecimal("20.00"), 100.0));
        ProductoVeterinarioEntity productoB = productoRepository.save(new ProductoVeterinarioEntity(
                "Antiparasitario costo real", "Medicamento", "ml", 0.1, 1.0,
                new BigDecimal("30.00"), 50.0));
        PlanSanitarioEntity plan = new PlanSanitarioEntity("Plan costo real", "Dos productos", 6);
        plan.getProductos().addAll(List.of(productoA, productoB));
        plan = planRepository.save(plan);
        InventarioEntity inventarioA = inventarioRepository.save(new InventarioEntity(
                productoA, "LOTE-COSTO-A", LocalDate.now().plusDays(30), 5, new BigDecimal("20.00")));
        InventarioEntity inventarioB = inventarioRepository.save(new InventarioEntity(
                productoB, "LOTE-COSTO-B", LocalDate.now().plusDays(45), 6, new BigDecimal("30.00")));
        JornadaSanitariaEntity jornada = jornadaRepository.save(new JornadaSanitariaEntity(
                LocalDate.now(), "Jornada con dos productos", 1, BigDecimal.ZERO, null, lote, plan));

        jornadaSanitariaService.ejecutar(jornada.getId(), new EjecutarJornadaEntrada(
                List.of(animal.getId()),
                List.of(new RegistroProductoJornada(productoA.getId(), 0.5, null),
                        new RegistroProductoJornada(productoB.getId(), 0.25, null))));

        assertThat(inventarioRepository.findById(inventarioA.getId()).orElseThrow().getCantidadDisponible())
                .isEqualTo(3);
        assertThat(inventarioRepository.findById(inventarioB.getId()).orElseThrow().getCantidadDisponible())
                .isEqualTo(4);

        var costo = costoJornadaService.procesarCostoJornada(new ProcesarCostoJornadaEntrada(
                jornada.getId(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        assertThat(costo.unidadesConsumidas()).isEqualTo(4);
        assertThat(costo.costoProducto()).isEqualByComparingTo("100.00");
        assertThat(costo.costoTotal()).isEqualByComparingTo("100.00");
        assertThat(inventarioRepository.findById(inventarioA.getId()).orElseThrow().getCantidadDisponible())
                .isEqualTo(3);
        assertThat(inventarioRepository.findById(inventarioB.getId()).orElseThrow().getCantidadDisponible())
                .isEqualTo(4);
    }

    @Test
        void apiVersionadaDevuelve201ConLocationYValidaSolicitud() throws Exception {
            String token = token("admin", "AdminLocal2026!");
            MvcResult lotResponse = mockMvc.perform(post("/api/v1/lotes")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("""
                                    {"nombre":"Lote API","ubicacion":"Finca","proposito":"Sanidad","activo":true}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andReturn();
            Long lotId = objectMapper.readTree(lotResponse.getResponse().getContentAsByteArray())
                    .get("id").asLong();

            mockMvc.perform(post("/api/v1/animales")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsBytes(java.util.Map.of(
                                    "codigoArete", "API-ANIMAL-01",
                                    "raza", "Brahman",
                                    "sexo", "Hembra",
                                    "pesoKg", 350.0,
                                    "fechaNacimiento", LocalDate.of(2024, 1, 1).toString(),
                                    "estado", "Activo",
                                    "loteId", lotId))))
                    .andExpect(status().isCreated());
            mockMvc.perform(get("/api/v1/animales?estado=Activo&raza=Brahman&page=0&size=5&sort=pesoKg,desc")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements").value(1))
                    .andExpect(jsonPath("$.page.size").value(5))
                    .andExpect(jsonPath("$.content[0].codigoArete").value("API-ANIMAL-01"));

            MvcResult productoResponse = mockMvc.perform(post("/api/v1/productos")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("""
                                    {"nombre":"API Producto","tipo":"Vacuna","unidadMedida":"ml",
                                     "dosisMinima":0.1,"dosisMaxima":1.0,
                                     "precioUnitario":2500.00,"contenidoMlPorUnidad":100.0,
                                     "retiroCarneDias":15,"retiroLecheDias":2}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.retiroCarneDias").value(15))
                    .andExpect(jsonPath("$.retiroLecheDias").value(2))
                    .andReturn();
            Long productoId = objectMapper.readTree(productoResponse.getResponse().getContentAsByteArray())
                    .get("id").asLong();
            mockMvc.perform(get("/api/v1/productos?precioMinimo=2000&precioMaximo=3000&page=0&size=5")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements").value(1));
            mockMvc.perform(post("/api/v1/planes-sanitarios")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsBytes(java.util.Map.of(
                                    "nombre", "API Plan sanitario",
                                    "descripcion", "Intervalo por API",
                                    "edadObjetivoMeses", 12,
                                    "intervaloMinimoDias", 14,
                                    "productosIds", List.of(productoId)))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.intervaloMinimoDias").value(14));

            mockMvc.perform(post("/api/v1/lotes")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("""
                                    {"nombre":" ","ubicacion":"Finca","proposito":"Sanidad","activo":true}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        }

    @Test
        void apiProtegidaDevuelve401SinTokenY403ParaRolInsuficiente() throws Exception {
            mockMvc.perform(get("/api/v1/lotes"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));

            String tokenVeterinario = token("veterinario", "VetLocal2026!");
            mockMvc.perform(get("/api/v1/admin/usuarios")
                            .header("Authorization", "Bearer " + tokenVeterinario))
                    .andExpect(status().isForbidden())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        }

    @Test
    void apiDevuelve404Y422ConProblemDetails() throws Exception {
            String token = token("admin", "AdminLocal2026!");
            mockMvc.perform(get("/api/v1/animales/999999")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));

            LoteEntity loteInactivo = new LoteEntity("Lote API inactivo", "Finca", "Sanidad");
            loteInactivo.setActivo(false);
            loteRepository.save(loteInactivo);
            mockMvc.perform(post("/api/v1/jornadas")
                            .header("Authorization", "Bearer " + token)
                            .contentType("application/json")
                            .content("""
                                    {"loteId":%d,"planId":1,"fecha":"%s","descripcion":"Prueba API"}
                                    """.formatted(loteInactivo.getId(), LocalDate.now())))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        }

    @Test
    void bitacoraValidaEntradaDevuelve201LocationYExponeDtoEnLaColeccion() throws Exception {
        String token = token("admin", "AdminLocal2026!");
        when(bitacoraRepository.save(any(BitacoraEvento.class))).thenAnswer(invocation -> {
            BitacoraEvento evento = invocation.getArgument(0);
            evento.setId("evento-api-1");
            return evento;
        });

        mockMvc.perform(post("/api/v1/bitacora")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"entidad":"Jornada","accion":"CREAR","usuario":"admin",
                                 "detalle":"Jornada registrada"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith(
                        "/api/v1/bitacora/evento-api-1")))
                .andExpect(jsonPath("$.id").value("evento-api-1"))
                .andExpect(jsonPath("$.entidad").value("Jornada"))
                .andExpect(jsonPath("$.fechaHora").isNotEmpty())
                .andReturn();
        verify(bitacoraRepository).save(any(BitacoraEvento.class));

        BitacoraEvento evento = new BitacoraEvento(
                "Jornada", "CREAR", "admin", "Jornada registrada", java.time.Instant.now());
        evento.setId("evento-api-1");
        when(bitacoraRepository.findById("evento-api-1")).thenReturn(Optional.of(evento));
        when(bitacoraRepository.findAll()).thenReturn(List.of(evento));

        mockMvc.perform(get("/api/v1/bitacora/evento-api-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("evento-api-1"));
        mockMvc.perform(get("/api/v1/bitacora")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("evento-api-1"))
                .andExpect(jsonPath("$[0].detalle").value("Jornada registrada"));

        mockMvc.perform(post("/api/v1/bitacora")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"entidad":" ","accion":"CREAR","usuario":"admin","detalle":"Detalle"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.errors.entidad[0]").exists())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("entidad")));
    }

    @Test
    void apiEliminaRecursoCon204SinContenido() throws Exception {
        String token = token("admin", "AdminLocal2026!");
        LoteEntity lote = loteRepository.save(new LoteEntity("Lote para eliminar por API", "Finca", "Sanidad"));

        mockMvc.perform(delete("/api/v1/lotes/{id}", lote.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(loteRepository.existsById(lote.getId())).isFalse();
    }

    @Test
    void apiDevuelve409ProblemDetailsAlCrearLoteDuplicado() throws Exception {
        String token = token("admin", "AdminLocal2026!");
        loteRepository.save(new LoteEntity("Lote con nombre repetido", "Finca", "Sanidad"));

        String response = mockMvc.perform(post("/api/v1/lotes")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"nombre":"Lote con nombre repetido","ubicacion":"Otra finca",
                                 "proposito":"Sanidad","activo":true}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(409))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).doesNotContain("trace", "stackTrace", "DataIntegrityViolationException");
        assertThat(loteRepository.count()).isEqualTo(1);
    }

        @Test
        void openApiEsPublicaYJornadaSoloEsVisibleParaSuPropietario() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"));
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());

            LoteEntity lote = loteRepository.save(new LoteEntity("Lote propietario", "Finca", "Sanidad"));
            animalRepository.save(new AnimalEntity("OWNER-01", "Brahman", "Hembra", 300.0,
                    LocalDate.of(2023, 1, 1), "Activo", lote));
            ProductoVeterinarioEntity producto = productoRepository.save(new ProductoVeterinarioEntity(
                    "Producto propietario", "Vacuna", "ml", 0.1, 1.0, new BigDecimal("20.00"), 100.0));
            PlanSanitarioEntity plan = new PlanSanitarioEntity("Plan propietario", "Plan API", 6);
            plan.getProductos().add(producto);
            plan = planRepository.save(plan);

            String veterinarianToken = token("veterinario", "VetLocal2026!");
            MvcResult createResponse = mockMvc.perform(post("/api/v1/jornadas")
                            .header("Authorization", "Bearer " + veterinarianToken)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsBytes(java.util.Map.of(
                                    "loteId", lote.getId(), "planId", plan.getId(),
                                    "fecha", LocalDate.now().toString(), "descripcion", "Jornada propia"))))
                    .andExpect(status().isCreated())
                    .andReturn();
            Long id = objectMapper.readTree(createResponse.getResponse().getContentAsByteArray())
                    .get("id").asLong();

            mockMvc.perform(get("/api/v1/jornadas/{id}", id)
                            .header("Authorization", "Bearer " + token("admin", "AdminLocal2026!")))
                    .andExpect(status().isNotFound());
            mockMvc.perform(get("/api/v1/jornadas/{id}", id)
                            .header("Authorization", "Bearer " + veterinarianToken))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/v1/jornadas?page=0&size=5&sort=fecha,desc")
                            .header("Authorization", "Bearer " + veterinarianToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(id));
            mockMvc.perform(get("/api/v1/jornadas?page=0&size=5&sort=fecha,desc")
                            .header("Authorization", "Bearer " + token("admin", "AdminLocal2026!")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements").value(0));
        }

        @Test
        void parametroDeOrdenamientoInvalidoDevuelve400ProblemDetails() throws Exception {
            mockMvc.perform(get("/api/v1/animales?sort=campoInexistente,asc")
                            .header("Authorization", "Bearer " + token("admin", "AdminLocal2026!")))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        }

        @Test
        void uiAdminValidaFormularioYAplicaPostRedirectGet() throws Exception {
            mockMvc.perform(get("/admin/productos"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));

            MockHttpSession session = loginWeb("admin", "AdminLocal2026!");
            mockMvc.perform(post("/admin/productos").session(session).with(csrf())
                            .param("nombre", "")
                            .param("tipo", "")
                            .param("unidadMedida", "")
                            .param("precioUnitario", "")
                            .param("contenidoMlPorUnidad", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("productos/form"))
                    .andExpect(model().attributeHasFieldErrors(
                            "form", "nombre", "tipo", "unidadMedida", "precioUnitario", "contenidoMlPorUnidad"));

            mockMvc.perform(post("/admin/productos").session(session).with(csrf())
                            .param("nombre", "Vacuna UI integración")
                            .param("tipo", "Vacuna")
                            .param("unidadMedida", "ml")
                            .param("dosisMinima", "0.1")
                            .param("dosisMaxima", "1.0")
                            .param("precioUnitario", "2500.00")
                            .param("contenidoMlPorUnidad", "100.0"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/productos"));

            mockMvc.perform(get("/admin/productos").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("productos/lista"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Vacuna UI integración")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(
                            "El producto veterinario se creó correctamente.")));
        }

        @Test
        void uiAdminEditaProductoYProtegeLaEscrituraConCsrf() throws Exception {
            MockHttpSession session = loginWeb("admin", "AdminLocal2026!");
            ProductoVeterinarioEntity producto = productoRepository.save(new ProductoVeterinarioEntity(
                    "Producto para editar UI", "Vacuna", "ml", 0.1, 1.0,
                    new BigDecimal("2000.00"), 50.0));

            mockMvc.perform(get("/admin/productos/{id}", producto.getId()).session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("productos/form"))
                    .andExpect(model().attributeExists("productoId", "form"));

            mockMvc.perform(post("/admin/productos/{id}", producto.getId()).session(session)
                            .param("nombre", "Producto editado UI")
                            .param("tipo", "Medicamento")
                            .param("unidadMedida", "ml")
                            .param("dosisMinima", "0.2")
                            .param("dosisMaxima", "2.0")
                            .param("precioUnitario", "3000.00")
                            .param("contenidoMlPorUnidad", "75.0"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/admin/productos/{id}", producto.getId()).session(session).with(csrf())
                            .param("nombre", "Producto editado UI")
                            .param("tipo", "Medicamento")
                            .param("unidadMedida", "ml")
                            .param("dosisMinima", "0.2")
                            .param("dosisMaxima", "2.0")
                            .param("precioUnitario", "3000.00")
                            .param("contenidoMlPorUnidad", "75.0"))
                    .andExpect(status().is3xxRedirection());

            assertThat(productoRepository.findById(producto.getId()).orElseThrow().getNombre())
                    .isEqualTo("Producto editado UI");
        }

        @Test
        void uiAdminValidaProveedorYAplicaPostRedirectGet() throws Exception {
            MockHttpSession session = loginWeb("admin", "AdminLocal2026!");

            mockMvc.perform(post("/admin/proveedores").session(session).with(csrf())
                            .param("nombre", "")
                            .param("telefono", "123")
                            .param("correo", "correo-invalido"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("proveedores/form"))
                    .andExpect(model().attributeHasFieldErrors("form", "nombre", "correo"));

            mockMvc.perform(post("/admin/proveedores").session(session)
                            .param("nombre", "Proveedor UI integración")
                            .param("telefono", "2222-3333")
                            .param("correo", "proveedor@example.test"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/admin/proveedores").session(session).with(csrf())
                            .param("nombre", "Proveedor UI integración")
                            .param("telefono", "2222-3333")
                            .param("correo", "proveedor@example.test"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/proveedores"));

            mockMvc.perform(get("/admin/proveedores").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("proveedores/lista"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(
                            "Proveedor UI integración")))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(
                            "El proveedor se creó correctamente.")));
        }

        @Test
        void uiRestringeCatalogoAAdminYEscapaContenidoDelProducto() throws Exception {
            MockHttpSession veterinario = loginWeb("veterinario", "VetLocal2026!");
            mockMvc.perform(get("/admin/productos").session(veterinario))
                    .andExpect(status().isForbidden());

            productoRepository.save(new ProductoVeterinarioEntity(
                    "Vacuna <script>alert(1)</script>", "Vacuna", "ml", 0.1, 1.0,
                    new BigDecimal("2000.00"), 50.0));
            MockHttpSession admin = loginWeb("admin", "AdminLocal2026!");

            mockMvc.perform(get("/admin/productos").session(admin))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(
                            "Vacuna &lt;script&gt;alert(1)&lt;/script&gt;")))
                    .andExpect(content().string(org.hamcrest.Matchers.not(
                            org.hamcrest.Matchers.containsString("<script>alert(1)</script>"))));
        }

        private MockHttpSession loginWeb(String username, String password) throws Exception {
            MvcResult response = mockMvc.perform(post("/login").with(csrf())
                            .param("username", username)
                            .param("password", password))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();
            return (MockHttpSession) response.getRequest().getSession(false);
        }

    private String token(String username, String password) throws Exception {
        MvcResult response = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(
                                java.util.Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(response.getResponse().getContentAsByteArray())
                .get("accessToken").asText();
    }
}
