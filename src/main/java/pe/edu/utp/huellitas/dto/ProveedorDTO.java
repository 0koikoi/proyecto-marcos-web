package pe.edu.utp.huellitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProveedorDTO {
    private Long id;

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener exactamente 11 dígitos numéricos")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(min = 2, max = 100, message = "La razón social debe tener entre 2 y 100 caracteres")
    private String razonSocial;

    @Size(max = 100, message = "El contacto no debe exceder los 100 caracteres")
    private String contacto;

    @Pattern(
        regexp = "^9\\d{8}$",
        message = "El teléfono debe tener 9 dígitos"
    )
    private String telefono;
}
