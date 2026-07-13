package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Historia clínica de un paciente.
 * Cada entrada representa una consulta veterinaria.
 *
 * Una historia clínica puede estar ligada a una cita previa (cita_id)
 * o ser una consulta directa sin cita previa (cita == null).
 */
@Data
@Entity
@Table(name = "historia_clinica")
public class HistoriaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El paciente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @NotNull(message = "El veterinario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;  // Veterinario tratante

    /** Cita que originó esta consulta. Puede ser nulo (walk-in). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @Column(nullable = false)
    private OffsetDateTime fechaConsulta = OffsetDateTime.now();

    @NotBlank(message = "El motivo de consulta es obligatorio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivoConsulta;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    /** Peso del animal en el momento de la consulta (kg). */
    @Column(precision = 5, scale = 2)
    private BigDecimal pesoKg;

    /** Temperatura corporal (°C). */
    @Column(precision = 4, scale = 1)
    private BigDecimal temperaturaC;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
