package pe.edu.utp.huellitas.model;

/**
 * Estados posibles de una solicitud de material.
 *
 * Flujo normal:
 *   PENDIENTE → APROBADA → ENTREGADA
 *   PENDIENTE → RECHAZADA
 *
 * Usado en la entidad {@link SolicitudMaterial} con @Enumerated(EnumType.STRING).
 * Compatible con el CHECK constraint del SQL:
 *   CHECK (estado IN ('PENDIENTE','APROBADA','RECHAZADA','ENTREGADA'))
 *
 * Reglas de negocio:
 *   - Solo ADMINISTRADOR puede cambiar el estado.
 *   - Al pasar a ENTREGADA, el servicio debe descontar stock del producto.
 */
public enum EstadoSolicitud {

    /** Solicitud creada por el veterinario, pendiente de revisión. */
    PENDIENTE,

    /** El administrador aprobó la solicitud. */
    APROBADA,

    /** El administrador rechazó la solicitud. Debe incluir observación. */
    RECHAZADA,

    /** El material fue entregado físicamente al veterinario. */
    ENTREGADA
}

