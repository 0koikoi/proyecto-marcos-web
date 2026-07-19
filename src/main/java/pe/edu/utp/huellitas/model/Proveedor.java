package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

/**
 * Proveedor de productos para la clínica.
 *
 * V1 SQL columns: id, ruc, razon_social, contacto, telefono, email, activo,
 *                 creado_en, creado_por, actualizado_en, actualizado_por
 */
@Data
@Entity
@Table(name = "proveedor")
public class Proveedor extends pe.edu.utp.huellitas.audit.Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener exactamente 11 dígitos")
    @Column(name = "ruc", unique = true, nullable = false, length = 11)
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(min = 2, max = 150, message = "La razón social debe tener entre 2 y 150 caracteres")
    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Size(max = 100, message = "El contacto no debe exceder los 100 caracteres")
    @Column(name = "contacto", length = 100)
    private String contacto;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @jakarta.validation.constraints.Email(message = "Formato de correo inválido")
    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;



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
