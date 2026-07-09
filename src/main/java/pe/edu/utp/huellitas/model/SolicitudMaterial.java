package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Solicitud de material/insumos realizada por un veterinario.
 * El administrador la aprueba, rechaza o marca como entregada.
 *
 * Flujo de estados:
 *   PENDIENTE → APROBADA → ENTREGADA
 *   PENDIENTE → RECHAZADA
 *
 * TODO para el equipo:
 *   - Implementar SolicitudMaterialController
 *   - Implementar SolicitudMaterialService
 *   - Implementar SolicitudMaterialRepository
 *   - Crear templates: solicitudes/lista.html, solicitudes/nueva.html
 *   - Al aprobar, descontar stock del producto (o aumentarlo si es reposición)
 */
@Data
@Entity
@Table(name = "solicitud_material")
public class SolicitudMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Veterinario que realiza la solicitud. */
    @NotNull
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

    @NotBlank(message = "El motivo es obligatorio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    /**
     * Estado actual de la solicitud.
     * Solo el ADMINISTRADOR puede cambiar el estado.
     */
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";  // PENDIENTE | APROBADA | RECHAZADA | ENTREGADA

    /** Administrador que responde la solicitud. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobado_por")
    private Personal aprobadoPor;

    @Column(columnDefinition = "TEXT")
    private String observacionRespuesta;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime fechaRespuesta;
}
