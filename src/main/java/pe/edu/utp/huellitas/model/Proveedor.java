package pe.edu.utp.huellitas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Entity
@Table(name = "proveedor")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(
    regexp = "\\d{11}",
    message = "El RUC debe tener exactamente 11 dígitos"
    )
    @Column(unique = true, nullable = false, length = 11)
    private String ruc;

    @Pattern(
    regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$",
    message = "Solo se permiten letras"
    )
    @Column(nullable = false)
    private String razonSocial;

    @Pattern(
    regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$",
    message = "Solo se permiten letras"
    )
    @Column(length = 100)
    private String contacto;

    @Pattern(
    regexp = "\\d{9}",
    message = "El teléfono debe tener 9 dígitos"
    )
    private String telefono;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private java.time.OffsetDateTime createdAt = java.time.OffsetDateTime.now();
}