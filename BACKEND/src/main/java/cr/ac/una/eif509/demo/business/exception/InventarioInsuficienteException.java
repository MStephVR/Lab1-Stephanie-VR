package cr.ac.una.eif509.demo.business.exception;

public class InventarioInsuficienteException extends ReglaNegocioException {

    public InventarioInsuficienteException(String producto, long disponible, int requerido) {
        super("Inventario insuficiente para " + producto
                + ": hay " + disponible + ", se requieren " + requerido + ".");
    }
}
