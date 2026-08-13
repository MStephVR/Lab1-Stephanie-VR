package cr.ac.una.eif509.demo.data;

import org.springframework.stereotype.Repository;

/**
 * CAPA DE DATOS.
 * Su responsabilidad es leer y escribir información de la fuente de datos.
 * Hoy devuelve valores fijos; más adelante esta capa puede hablar con una
 * base de datos para guardar animales, vacunas, desparasitaciones y cobros.
 *
 * Esta es la capa más "adentro": no depende de ninguna otra capa del sistema.
 */
@Repository
public class ControlGanadoRepository {

    public String leerEstadoDelSistema() {
        return "Sistema de control de ganado listo";
    }

    public int costoVacunacionPorAnimal() {
        return 4500;
    }

    public int costoDesparasitacionPorAnimal() {
        return 3000;
    }
}