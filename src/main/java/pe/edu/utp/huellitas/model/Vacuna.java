package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Registro de vacunación de un paciente.
 *
 * V1 SQL columns: id, paciente_id, historia_clinica_id, personal_id,
 *                 nombre, lote, fecha_aplicacion, fecha_proxima_dosis
 */
@Data
@Entity
@Table(name = "vacuna")
public class Vacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El paciente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    /** Relación opcional para trazabilidad clínica completa. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historia_clinica_id")
    private HistoriaClinica historiaClinica;

    @NotNull(message = "El veterinario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @NotBlank(message = "El nombre de la vacuna es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "lote", length = 50)
    private String lote;

    @NotNull(message = "La fecha de aplicación es obligatoria")
    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fechaAplicacion;

    /** Fecha programada para la siguiente dosis. Puede ser null. */
    @Column(name = "fecha_proxima_dosis")
    private LocalDate fechaProximaDosis;
}
