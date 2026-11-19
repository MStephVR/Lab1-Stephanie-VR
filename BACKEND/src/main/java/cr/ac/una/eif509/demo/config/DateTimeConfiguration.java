package cr.ac.una.eif509.demo.config;

import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class DateTimeConfiguration {
    private static final ZoneId APPLICATION_ZONE = ZoneId.of("America/Costa_Rica");

    @Bean
    Clock applicationClock() {
        return Clock.system(APPLICATION_ZONE);
    }

    @Bean
    ValidationConfigurationCustomizer validationClockProvider(Clock applicationClock) {
        return configuration -> configuration.clockProvider(() -> applicationClock);
    }
}
