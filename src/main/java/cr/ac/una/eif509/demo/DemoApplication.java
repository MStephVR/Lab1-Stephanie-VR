package cr.ac.una.eif509.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 * La anotación @SpringBootApplication activa la configuración automática de Spring
 * y le dice que busque componentes (controladores, servicios, repositorios)
 * a partir de este paquete hacia abajo.
 */
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
