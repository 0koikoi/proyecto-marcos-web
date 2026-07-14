package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "proveedor")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener exactamente 11 dígitos")
    @Column(unique = true, nullable = false, length = 11)
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(min = 2, max = 100, message = "La razón social debe tener entre 2 y 100 caracteres")
    @Column(nullable = false)
    private String razonSocial;

    @Size(max = 100, message = "El contacto no debe exceder los 100 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$", message = "Solo se permiten letras")
    @Column(length = 100)
    private String contacto;

    @Pattern(regexp = "^(9\\d{8}|[0-9]{6,15})?$", message = "El teléfono no es válido")
    @Pattern(regexp = "^(\\+51\\s?)?9\\d{8}$", message = "El teléfono debe tener formato 912345678 o +51 912345678")
    @Column(length = 15)
    private String telefono;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

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
            return this.telefono.substring(4); // Remover "+51 "
        }
        return this.telefono;
    }

    public String getTelefonoCompleto() {
        return this.telefono;
    }
}