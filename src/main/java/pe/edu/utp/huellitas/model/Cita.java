package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

/**
 * Cita veterinaria agendada por recepción.
 *
 * Una cita puede generar una HistoriaClinica al ser atendida.
 * El veterinario asignado es el responsable de atender la consulta.
 *
 * Flujo de estados:
 *   PENDIENTE → EN_PROCESO → COMPLETADA
 *   PENDIENTE → CANCELADA
 *
 * Permisos:
 *   - Ver:     todos los roles autenticados
 *   - Crear/Editar: ADMINISTRADOR + RECEPCION
 *   - Cancelar: ADMINISTRADOR + RECEPCION
 *   - Eliminar: solo ADMINISTRADOR
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "cita")
public class Cita extends pe.edu.utp.huellitas.audit.Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Debe seleccionar un paciente")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @NotNull(message = "Debe seleccionar un veterinario")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @NotNull(message = "La fecha y hora son obligatorias")
    @FutureOrPresent(message = "La cita no puede programarse en el pasado")
    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;

    /** Duración estimada de la cita en minutos. Mínimo 1 minuto. */
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos = 30;

    @Column(name = "motivo", length = 200)
    private String motivo;

    /**
     * Estado actual de la cita.
     * Se persiste como String en la BD (EnumType.STRING).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;


}

