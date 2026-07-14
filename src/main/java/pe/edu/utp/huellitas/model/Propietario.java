package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import pe.edu.utp.huellitas.validation.DniPeruano;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Propietario (cliente) de los pacientes de la clínica.
 * Identificado de forma única por su DNI peruano (8 dígitos).
 *
 * Permisos:
 *   - Ver/Crear/Editar: todos los roles autenticados
 *   - Eliminar: solo ADMINISTRADOR
 */
@Data
@Entity
@Table(name = "propietario")
public class Propietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DniPeruano
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener solo números y tener exactamente 8 dígitos")
    @Column(name = "dni", length = 8, nullable = false, unique = true)
    private String dni;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
        regexp = "^(\\+51\\s?)?9\\d{8}$",
        message = "El teléfono debe tener formato 912345678 o +51 912345678"
    )
    @Column(name = "telefono", length = 15, nullable = false)
    private String telefono;

    @Email(message = "Debe ser un correo electrónico válido")
    @Column(name = "correo", length = 150)
    private String correo;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    /** Auditoría: cuándo fue registrado el propietario. */
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Auditoría: última actualización. */
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /**
     * Mascotas del propietario.
     * CascadeType.ALL: si se elimina el propietario, se eliminan sus pacientes.
     * ADVERTENCIA: el servicio debe verificar si tiene historia clínica antes de eliminar.
     */
    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paciente> pacientes = new ArrayList<>();
}