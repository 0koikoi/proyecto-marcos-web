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
 * TODO para el equipo:
 *   - Implementar VacunaController
 *   - Implementar VacunaService
 *   - Implementar VacunaRepository
 *   - Crear template: vacunas/lista.html, vacunas/formulario.html
 *   - Mostrar alerta en dashboard cuando fecha_proxima_dosis esté próxima (< 7 días)
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

    @NotNull(message = "El veterinario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @NotBlank(message = "El nombre de la vacuna es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombreVacuna;

    @Column(length = 100)
    private String laboratorio;

    @Column(length = 50)
    private String lote;

    @NotNull(message = "La fecha de aplicación es obligatoria")
    @Column(nullable = false)
    private LocalDate fechaAplicacion;

    /** Fecha programada para la siguiente dosis. Puede ser null. */
    private LocalDate fechaProximaDosis;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
