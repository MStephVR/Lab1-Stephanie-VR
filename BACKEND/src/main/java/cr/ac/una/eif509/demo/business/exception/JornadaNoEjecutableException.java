package cr.ac.una.eif509.demo.business.exception;

public class JornadaNoEjecutableException extends ReglaNegocioException {

    public JornadaNoEjecutableException(String estado) {
        super("La jornada debe estar planificada para ejecutarse; estado actual: " + estado + ".");
    }
}
