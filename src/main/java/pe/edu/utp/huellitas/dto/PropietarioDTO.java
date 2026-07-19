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

    @NotBlank(message = "Los nombres son obligatorios")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ. ]+$", message = "Los nombres solo pueden contener letras")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ. ]+$", message = "Los apellidos solo pueden contener letras")
    private String apellidos;

    @Pattern(
        regexp = "^9\\d{8}$",
        message = "El teléfono debe tener 9 dígitos"
    )
    private String telefono;

    @Email(message = "Debe ser un correo electrónico válido")
    private String email;

    private String direccion;

    // Helper para compatibilidad con vistas existentes
    public String getNombreCompleto() {
        String n = nombres != null ? nombres : "";
        String a = apellidos != null ? apellidos : "";
        return (n + " " + a).trim();
    }
}
