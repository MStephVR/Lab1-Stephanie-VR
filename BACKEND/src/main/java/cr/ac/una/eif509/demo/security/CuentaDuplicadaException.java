package cr.ac.una.eif509.demo.security;

public class CuentaDuplicadaException extends RuntimeException {
    public CuentaDuplicadaException() {
        super("Ya existe una cuenta con ese nombre de usuario.");
    }
}
