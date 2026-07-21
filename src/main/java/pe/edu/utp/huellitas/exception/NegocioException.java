package pe.edu.utp.huellitas.exception;

/**
 * Excepción de negocio genérica del sistema Huellitas.
 *
 * Se lanza desde los servicios cuando una operación viola una regla de negocio,
 * por ejemplo:
 *   - Intentar anular una venta ya anulada
 *   - Stock insuficiente al marcar entrega de solicitud
 *   - DNI duplicado al registrar un propietario
 *
 * El {@link GlobalExceptionHandler} captura esta excepción y la muestra al usuario
 * de forma amigable en la vista, sin stack trace.
 *
 * Uso recomendado en servicios:
 * <pre>
 *   throw new NegocioException("El stock es insuficiente para esta operación.");
 * </pre>
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String message) {
        super(message);
    }

    public NegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
