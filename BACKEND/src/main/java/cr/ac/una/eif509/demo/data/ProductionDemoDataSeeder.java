package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.JornadaSanitariaEntity;
import cr.ac.una.eif509.demo.domain.LoteEntity;
import cr.ac.una.eif509.demo.domain.PlanSanitarioEntity;
import cr.ac.una.eif509.demo.domain.ProductoVeterinarioEntity;
import cr.ac.una.eif509.demo.domain.UsuarioEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@Profile("prod")
@ConditionalOnProperty(prefix = "sisganado.demo-data", name = "enabled", havingValue = "true")
@Order(2)
public class ProductionDemoDataSeeder implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionDemoDataSeeder.class);
    private static final ZoneId COSTA_RICA = ZoneId.of("America/Costa_Rica");

    private final LoteRepository loteRepository;
    private final AnimalRepository animalRepository;
    private final ProductoVeterinarioRepository productoRepository;
    private final PlanSanitarioRepository planRepository;
    private final JornadaSanitariaRepository jornadaRepository;
    private final UsuarioRepository usuarioRepository;

    public ProductionDemoDataSeeder(
            LoteRepository loteRepository,
            AnimalRepository animalRepository,
            ProductoVeterinarioRepository productoRepository,
            PlanSanitarioRepository planRepository,
            JornadaSanitariaRepository jornadaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.loteRepository = loteRepository;
        this.animalRepository = animalRepository;
        this.productoRepository = productoRepository;
        this.planRepository = planRepository;
        this.jornadaRepository = jornadaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        UsuarioEntity admin = usuarioRepository.findByUsername("admin")
                .orElseThrow(() -> new IllegalStateException("No se encontró la cuenta admin para cargar ejemplos."));

        LoteEntity loteDemo = obtenerLote("Lote Demo Presentación", "Finca San Isidro", "Crianza y engorde");
        LoteEntity loteLechero = obtenerLote("Lote Lechero", "Finca San Isidro", "Producción de leche");
        LoteEntity loteCria = obtenerLote("Lote de Cría", "Finca San Isidro", "Cría y desarrollo");

        ProductoVeterinarioEntity vacuna = obtenerProducto(
                "Vacuna demostrativa", "Vacuna", 1.0, 2.0, "5000.00");
        ProductoVeterinarioEntity desparasitante = obtenerProducto(
                "Desparasitante bovino", "Tratamiento veterinario", 1.0, 2.0, "6500.00");
        ProductoVeterinarioEntity vitaminas = obtenerProducto(
                "Vitaminas ADE", "Tratamiento veterinario", 1.0, 3.0, "4800.00");

        PlanSanitarioEntity planVacunacion = obtenerPlan(
                "Vacunación preventiva bovina", "Jornada demostrativa de vacunación del hato.", vacuna);
        PlanSanitarioEntity planDesparasitacion = obtenerPlan(
                "Desparasitación semestral", "Control preventivo de parásitos para el ganado adulto.",
                desparasitante);
        PlanSanitarioEntity planVitaminas = obtenerPlan(
                "Refuerzo vitamínico", "Suplementación vitamínica para fortalecer el hato.", vitaminas);

        obtenerAnimal("DEMO-001", "Brahman", "Hembra", 350.0, LocalDate.of(2022, 5, 10), loteDemo);
        obtenerAnimal("DEMO-002", "Holstein", "Hembra", 420.0, LocalDate.of(2021, 8, 15), loteLechero);
        obtenerAnimal("DEMO-003", "Brahman", "Macho", 310.0, LocalDate.of(2023, 2, 20), loteCria);

        LocalDate hoy = LocalDate.now(COSTA_RICA);
        obtenerJornada("Jornada demostrativa de vacunación del hato.", hoy, admin, loteDemo, planVacunacion);
        obtenerJornada("Jornada de desparasitación del lote lechero.", hoy, admin, loteLechero,
                planDesparasitacion);
        obtenerJornada("Refuerzo vitamínico para animales jóvenes.", hoy, admin, loteCria, planVitaminas);

        LOGGER.info("Datos de demostración verificados para Render: 3 lotes, 3 productos, 3 planes, "
                + "3 animales y 3 jornadas.");
    }

    private LoteEntity obtenerLote(String nombre, String ubicacion, String proposito) {
        return loteRepository.findByNombre(nombre).orElseGet(() -> {
            LoteEntity lote = new LoteEntity(nombre, ubicacion, proposito);
            lote.setActivo(true);
            return loteRepository.save(lote);
        });
    }

    private ProductoVeterinarioEntity obtenerProducto(
            String nombre, String tipo, double dosisMinima, double dosisMaxima, String precio
    ) {
        return productoRepository.findByNombre(nombre).orElseGet(() -> {
            ProductoVeterinarioEntity producto = new ProductoVeterinarioEntity(
                    nombre, tipo, "ml", dosisMinima, dosisMaxima, new BigDecimal(precio), 100.0);
            producto.setRetiroCarneDias(0);
            producto.setRetiroLecheDias(0);
            return productoRepository.save(producto);
        });
    }

    private PlanSanitarioEntity obtenerPlan(String nombre, String descripcion, ProductoVeterinarioEntity producto) {
        return planRepository.findByNombre(nombre).orElseGet(() -> {
            PlanSanitarioEntity plan = new PlanSanitarioEntity(nombre, descripcion, 12, 7);
            plan.getProductos().add(producto);
            return planRepository.save(plan);
        });
    }

    private AnimalEntity obtenerAnimal(
            String codigo, String raza, String sexo, double peso, LocalDate nacimiento, LoteEntity lote
    ) {
        return animalRepository.findByCodigoArete(codigo).orElseGet(() -> animalRepository.save(
                new AnimalEntity(codigo, raza, sexo, peso, nacimiento, "Activo", lote)));
    }

    private void obtenerJornada(
            String descripcion, LocalDate fecha, UsuarioEntity admin, LoteEntity lote, PlanSanitarioEntity plan
    ) {
        if (!jornadaRepository.existsByDescripcion(descripcion)) {
            jornadaRepository.save(new JornadaSanitariaEntity(
                    fecha, descripcion, 1, BigDecimal.ZERO, admin, lote, plan));
        }
    }
}
