package cr.ac.una.eif509.demo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventLogger implements ApplicationListener<AbstractAuthenticationEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEventLogger.class);

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        var authentication = event.getAuthentication();
        var logger = event instanceof AbstractAuthenticationFailureEvent
                ? LOGGER.atWarn()
                : LOGGER.atInfo();
        logger.addKeyValue("security_event", event.getClass().getSimpleName())
                .addKeyValue("principal", authentication.getName())
                .log("security authentication event");
    }
}
