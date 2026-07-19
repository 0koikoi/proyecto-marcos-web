package pe.edu.utp.huellitas.model;

/**
 * Tipos de pago aceptados en ventas.
 *
 * Usado en la entidad {@link Venta} con @Enumerated(EnumType.STRING).
 * Compatible con el CHECK constraint del SQL:
 *   CHECK (tipo_pago IN ('EFECTIVO','TARJETA','TRANSFERENCIA','MIXTO'))
 */
public enum TipoPago {

    /** Pago en efectivo en mostrador. */
    EFECTIVO,

    /** Pago con tarjeta de débito o crédito. */
    TARJETA,

    /** Transferencia bancaria o pago por aplicación. */
    TRANSFERENCIA,

    /** Combinación de dos o más métodos (ej: parte efectivo, parte tarjeta). */
    MIXTO
}

