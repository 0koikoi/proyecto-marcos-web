package pe.edu.utp.huellitas.model;

/**
 * Estados posibles de una cita veterinaria.
 *
 * Flujo normal:
 *   PENDIENTE → EN_PROCESO → COMPLETADA
 *   PENDIENTE → CANCELADA  (recepción o admin)
 *
 * Usado en la entidad {@link Cita} con @Enumerated(EnumType.STRING).
 * Compatible con el CHECK constraint del SQL:
 *   CHECK (estado IN ('PENDIENTE','EN_PROCESO','COMPLETADA','CANCELADA'))
 */
public enum EstadoCita {

    /** Cita agendada, aún no atendida. Estado inicial por defecto. */
    PENDIENTE,

    /** El veterinario está atendiendo al paciente en este momento. */
    EN_PROCESO,

    /** La consulta finalizó correctamente. */
    COMPLETADA,

    /** La cita fue cancelada por recepción o administrador. */
    CANCELADA
}

