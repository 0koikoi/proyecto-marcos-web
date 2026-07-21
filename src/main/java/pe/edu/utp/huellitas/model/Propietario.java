package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
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
 *   - Eliminar: solo ADMINISTRADOR (con validación de historial)
 */
@Data
@Entity
@Table(name = "propietario")
public class Propietario extends pe.edu.utp.huellitas.audit.Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DniPeruano
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener solo números y tener exactamente 8 dígitos")
    @Column(name = "dni", length = 8, nullable = false, unique = true)
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
        regexp = "^(\\+51\\s?)?9\\d{8}$",
        message = "El teléfono debe tener formato 912345678 o +51 912345678"
    )
    @Column(name = "telefono", length = 20, nullable = false)
    private String telefono;

    @jakarta.validation.constraints.Email(message = "Debe ser un correo electrónico válido")
    @Column(name = "email", length = 150)
    private String email;

    @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres")
    @Column(name = "direccion", length = 200)
    private String direccion;



    /**
     * Mascotas del propietario.
     * ADVERTENCIA: el servicio debe verificar historial antes de eliminar al propietario.
     */
    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paciente> pacientes = new ArrayList<>();

    // --- Helper ---

    @Transient
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public void setTelefono(String telefono) {
        if (telefono != null) {
            telefono = telefono.trim();
            if (telefono.matches("^9\\d{8}$")) {
                this.telefono = "+51 " + telefono;
            } else {
                this.telefono = telefono;
            }
        } else {
            this.telefono = null;
        }
    }

    public String getTelefono() {
        if (this.telefono != null && this.telefono.startsWith("+51 ")) {
            return this.telefono.substring(4);
        }
        return this.telefono;
    }

    public String getTelefonoCompleto() {
        return this.telefono;
    }
}
