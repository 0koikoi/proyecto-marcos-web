package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
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
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
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

    /** Fecha y hora de la consulta. Mapeada a la columna 'fecha' del SQL. */
    @Column(name = "fecha", nullable = false)
    private OffsetDateTime fecha = OffsetDateTime.now();

    /** Peso del animal en el momento de la consulta (kg). Columna: peso_kg */
    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    /** Temperatura corporal (°C). Columna: temperatura */
    @Column(name = "temperatura", precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "tratamiento", columnDefinition = "TEXT")
    private String tratamiento;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // --- Auditoría ---

    @org.springframework.data.annotation.CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @org.springframework.data.annotation.CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", updatable = false)
    private Personal creadoPor;
}
