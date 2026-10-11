package cr.ac.una.eif509.demo.business.exception;

public class RecursoNoEncontradoException extends ReglaNegocioException {
    public RecursoNoEncontradoException(String recurso) {
        super(recurso + " no existe o no está disponible.");
    }
}
