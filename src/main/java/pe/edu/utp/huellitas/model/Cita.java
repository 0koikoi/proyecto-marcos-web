package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cita")
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha y hora son obligatorias")
    @FutureOrPresent(message = "La cita no puede programarse en el pasado")
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @NotBlank(message = "El motivo de la cita es obligatorio")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Pattern(regexp = "PENDIENTE|EN_PROCESO|COMPLETADA|CANCELADA",
             message = "Estado inválido")
    @Column(length = 20)
    private String estado = "PENDIENTE";

    @NotNull(message = "Debe seleccionar un paciente")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @NotNull(message = "Debe seleccionar un veterinario")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;
}