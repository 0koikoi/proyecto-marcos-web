package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
@Entity
@Table(name = "cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha y hora son obligatorias")
    @FutureOrPresent(message = "La cita no puede programarse en el pasado")
    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;

    @NotBlank(message = "El motivo de la cita es obligatorio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    /**
     * Estado actual de la cita.
     * Se persiste como String en la BD (EnumType.STRING).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @NotNull(message = "Debe seleccionar un paciente")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @NotNull(message = "Debe seleccionar un veterinario")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    /** Auditoría: cuándo se creó el registro. */
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Auditoría: quién agendó la cita (recepcionista).
     * Puede ser null si fue creada por el sistema o migración.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Personal createdBy;
}