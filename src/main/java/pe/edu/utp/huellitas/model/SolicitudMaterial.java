package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Solicitud de material o insumos realizada por un veterinario.
 * El administrador la aprueba, rechaza o marca como entregada.
 *
 * Flujo de estados:
 *   PENDIENTE → APROBADA → ENTREGADA
 *   PENDIENTE → RECHAZADA
 *
 * Permisos:
 *   - Crear:                      VETERINARIO
 *   - Ver todas:                  ADMINISTRADOR
 *   - Ver las propias:            VETERINARIO
 *   - Aprobar/Rechazar/Entregar:  solo ADMINISTRADOR
 *
 * V1 SQL columns: id, personal_id_solicitante, producto_id, cantidad,
 *                 motivo, estado, personal_id_respuesta,
 *                 fecha_solicitud, fecha_respuesta
 */
@Data
@Entity
@Table(name = "solicitud_material")
public class SolicitudMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Veterinario que realiza la solicitud. Columna: personal_id_solicitante */
    @NotNull(message = "El solicitante es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id_solicitante", nullable = false)
    private Personal solicitante;

    @NotNull(message = "Debe seleccionar un producto")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    /**
     * Estado actual de la solicitud.
     * Solo el ADMINISTRADOR puede cambiar el estado.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    /** Personal que responde la solicitud (admin). Columna: personal_id_respuesta */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id_respuesta")
    private Personal personalRespuesta;

    /** Fecha en que se realizó la solicitud. */
    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private OffsetDateTime fechaSolicitud = OffsetDateTime.now();

    /** Fecha en que el administrador respondió. */
    @Column(name = "fecha_respuesta")
    private OffsetDateTime fechaRespuesta;
}
