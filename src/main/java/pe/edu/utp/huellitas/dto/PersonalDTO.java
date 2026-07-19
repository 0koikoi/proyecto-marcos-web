package pe.edu.utp.huellitas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pe.edu.utp.huellitas.model.Rol;

@Data
public class PersonalDTO {
    private Long id;

    // Se genera automáticamente en el backend
    private String codigoInstitucional;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    @Pattern(
        regexp = "^9\\d{8}$",
        message = "El teléfono debe tener 9 dígitos"
    )
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String email;

    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    // Helper para compatibilidad con vistas que usen nombreCompleto (ej. tablas)
    public String getNombreCompleto() {
        String n = nombre != null ? nombre : "";
        String a = apellido != null ? apellido : "";
        return (n + " " + a).trim();
    }
}
