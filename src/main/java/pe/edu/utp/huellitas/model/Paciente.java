package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Paciente (mascota) registrado en la clínica.
 * Siempre pertenece a un {@link Propietario}.
 *
 * Permisos:
 *   - Ver/Crear/Editar: todos los roles autenticados
 *   - Eliminar: solo ADMINISTRADOR (con validación de historial)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "paciente")
public class Paciente extends pe.edu.utp.huellitas.audit.Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Debe seleccionar un propietario")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Propietario propietario;

    @NotBlank(message = "El nombre del paciente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    @Column(name = "especie", length = 30, nullable = false)
    private String especie;

    @Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$",
        message = "La raza solo debe contener letras y espacios"
    )
    @Column(name = "raza", length = 100)
    private String raza;

    /** Género del paciente. Valores válidos: MACHO, HEMBRA. */
    @Column(name = "genero", length = 10)
    private String genero;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser en el futuro")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    /**
     * Si la fecha de nacimiento es estimada (mascota adoptada/encontrada).
     * Cuando es true, no se fuerza una fecha exacta.
     */
    @Column(name = "fecha_nacimiento_estimada", nullable = false)
    private Boolean fechaNacimientoEstimada = false;

    /**
     * Peso de referencia actualizado automáticamente desde la última HistoriaClinica.
     * NUNCA editar a mano en el formulario de paciente.
     */
    @Column(name = "peso_referencia", precision = 5, scale = 2)
    private BigDecimal pesoReferencia;

    @Column(name = "esterilizado", nullable = false)
    private Boolean esterilizado = false;

    @Column(name = "alergias", columnDefinition = "TEXT")
    private String alergias;

    /**
     * Estado del paciente.
     * Compatible con el CHECK constraint de la BD: ACTIVO, FALLECIDO, INACTIVO.
     */
    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "ACTIVO";


}

