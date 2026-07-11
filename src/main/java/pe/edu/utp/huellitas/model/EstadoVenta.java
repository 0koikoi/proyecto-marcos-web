package pe.edu.utp.huellitas.model;

/**
 * Estados posibles de una venta/boleta.
 *
 * Usado en la entidad {@link Venta} con @Enumerated(EnumType.STRING).
 * Compatible con el CHECK constraint del SQL:
 *   CHECK (estado IN ('PENDIENTE','PAGADA','ANULADA'))
 *
 * Reglas de negocio:
 *   - Solo ADMINISTRADOR puede anular una venta.
 *   - Una venta ANULADA debe revertir el stock de los productos.
 */
public enum EstadoVenta {

    /** Venta registrada pero aún sin cobrar (crédito / cuota pendiente). */
    PENDIENTE,

    /** Venta cobrada exitosamente. Estado por defecto. */
    PAGADA,

    /** Venta anulada. Solo el administrador puede realizarlo. */
    ANULADA
}
