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
 *   - Crear:                    VETERINARIO
 *   - Ver todas:                ADMINISTRADOR
 *   - Ver las propias:          VETERINARIO
 *   - Aprobar/Rechazar/Entregar: solo ADMINISTRADOR
 *
 * Lógica pendiente de implementar:
 *   Al marcar como ENTREGADA, el servicio debe descontar del stock del producto
 *   la cantidad indicada en {@code cantidadEntregada} (o {@code cantidadSolicitada}
 *   si se entrega el total).
 */
@Data
@Entity
@Table(name = "solicitud_material")
public class SolicitudMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Veterinario que realiza la solicitud. */
    @NotNull(message = "El solicitante es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Personal solicitante;

    @NotNull(message = "Debe seleccionar un producto")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    @Column(nullable = false)
    private Integer cantidadSolicitada;

    /**
     * Cantidad realmente entregada por el administrador.
     * Puede ser menor a {@code cantidadSolicitada} en aprobaciones parciales.
     * Se establece al marcar la solicitud como ENTREGADA.
     */
    @Column
    private Integer cantidadEntregada;

    @NotBlank(message = "El motivo es obligatorio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    /**
     * Estado actual de la solicitud.
     * Solo el ADMINISTRADOR puede cambiar el estado.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    /** Administrador que responde la solicitud. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private Personal aprobadoPor;

    /** Observación o motivo de rechazo del administrador. */
    @Column(columnDefinition = "TEXT")
    private String observacionRespuesta;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Fecha en que el administrador respondió (aprobó, rechazó o entregó). */
    private OffsetDateTime fechaRespuesta;
}
