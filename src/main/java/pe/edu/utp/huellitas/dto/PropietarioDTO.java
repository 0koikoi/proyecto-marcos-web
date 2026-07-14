package pe.edu.utp.huellitas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import pe.edu.utp.huellitas.validation.DniPeruano;

@Data
public class PropietarioDTO {
    private Long id;

    @DniPeruano
    private String dni;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ. ]+$", message = "El nombre solo puede contener letras")
    private String nombreCompleto;

    @Pattern(
        regexp = "^9\\d{8}$",
        message = "El teléfono debe tener 9 dígitos"
    )
    private String telefono;

    @Email(message = "Debe ser un correo electrónico válido")
    private String correo;

    private String direccion;
}
