package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Paciente (mascota) registrado en la clínica.
 * Siempre pertenece a un {@link Propietario}.
 *
 * Permisos:
 *   - Ver/Crear/Editar: todos los roles autenticados
 *   - Eliminar: solo ADMINISTRADOR
 */
@Data
@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del paciente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$",
        message = "El nombre solo debe contener letras y espacios"
    )
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    @Column(name = "especie", length = 50, nullable = false)
    private String especie;

    @Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$",
        message = "La raza solo debe contener letras y espacios"
    )
    @Column(name = "raza", length = 50)
    private String raza;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser en el futuro")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    /**
     * Género del paciente. Valores válidos: MACHO, HEMBRA, DESCONOCIDO.
     * Compatible con el CHECK constraint de la BD.
     */
    @Pattern(regexp = "^(MACHO|HEMBRA|DESCONOCIDO)$", message = "El género debe ser MACHO, HEMBRA o DESCONOCIDO")
    @Column(name = "genero", length = 10)
    private String genero;

    @NotNull(message = "Debe seleccionar un propietario")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Propietario propietario;

    /** Auditoría: cuándo fue registrado el paciente. */
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Auditoría: última actualización del registro. */
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /**
     * Auditoría: quién registró al paciente (recepcionista o admin).
     * Nullable para registros migrados o creados por el sistema.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Personal createdBy;
}