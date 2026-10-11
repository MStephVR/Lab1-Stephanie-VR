package cr.ac.una.eif509.demo.business.exception;

public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String message) {
        super(message);
    }

    public ReglaNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
