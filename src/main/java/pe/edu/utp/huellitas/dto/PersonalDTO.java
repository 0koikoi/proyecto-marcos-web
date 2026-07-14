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

    @NotBlank(message = "El código institucional es obligatorio")
    private String codigoInstitucional;

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(
        regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ. ]+$",
        message = "El nombre solo puede contener letras"
    )
    private String nombreCompleto;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol; // Se puede mapear directo desde select en el form

    @NotBlank(message = "El cargo es obligatorio")
    private String cargo;

    private String especialidad;

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
}
