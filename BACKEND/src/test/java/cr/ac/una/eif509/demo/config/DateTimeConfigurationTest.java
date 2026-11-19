package cr.ac.una.eif509.demo.config;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.FutureOrPresent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeConfigurationTest {
    @Test
    void futureOrPresentUsaLaFechaActualDeCostaRica() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-11-19T02:00:00Z"),
                ZoneId.of("America/Costa_Rica"));
        ValidationConfigurationCustomizer customizer =
                new DateTimeConfiguration().validationClockProvider(clock);
        var configuration = Validation.byDefaultProvider().configure();
        customizer.customize(configuration);

        try (ValidatorFactory factory = configuration.buildValidatorFactory()) {
            assertTrue(factory.getValidator().validate(new FechaEntrada(LocalDate.of(2026, 11, 18))).isEmpty());
        }
    }

    private record FechaEntrada(@FutureOrPresent LocalDate fecha) {
    }
}
