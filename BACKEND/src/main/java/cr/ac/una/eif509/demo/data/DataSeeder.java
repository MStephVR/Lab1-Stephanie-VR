package cr.ac.una.eif509.demo.data;

import cr.ac.una.eif509.demo.domain.AnimalEntity;
import cr.ac.una.eif509.demo.domain.BitacoraEvento;
import cr.ac.una.eif509.demo.domain.LoteEntity;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

@Component
@Profile({"local", "docker"})
public class DataSeeder implements ApplicationRunner {

    private final LoteRepository loteRepository;
    private final AnimalRepository animalRepository;
    private final BitacoraRepository bitacoraRepository;

    public DataSeeder(LoteRepository loteRepository,
                      AnimalRepository animalRepository,
                      BitacoraRepository bitacoraRepository) {
        this.loteRepository = loteRepository;
        this.animalRepository = animalRepository;
        this.bitacoraRepository = bitacoraRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (loteRepository.count() == 0) {
            LoteEntity lote = loteRepository.save(new LoteEntity("Lote Norte", "Finca San Isidro", "Crianza y engorde"));

            animalRepository.save(new AnimalEntity(
                    "A-001",
                    "Holstein",
                    "Macho",
                    420.0,
                    LocalDate.of(2023, 1, 15),
                    "Activo",
                    lote
            ));

            animalRepository.save(new AnimalEntity(
                    "A-002",
                    "Brahman",
                    "Hembra",
                    390.0,
                    LocalDate.of(2022, 11, 10),
                    "Activo",
                    lote
            ));
        }

        if (bitacoraRepository.count() == 0) {
            bitacoraRepository.save(new BitacoraEvento(
                    "JornadaSanitaria",
                    "CREADA",
                    "admin",
                    "Se inicializa la jornada de vacunación para el lote Norte",
                    Instant.now()
            ));
        }
    }
}
