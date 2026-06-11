package pe.edu.utp.huellitas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pe.edu.utp.huellitas.validation.DniPeruano;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "propietario")
public class Propietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DniPeruano
    @NotBlank(message = "El DNI es obligatorio")
    @Column(name = "dni", length = 8, nullable = false, unique = true)
    private String dni;

    @Getter
    @Setter
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @NotBlank(message = "El teléfono es obligatorio")
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^(\\+51\\s?)?9\\d{8}$", message = "El teléfono debe ser válido y empezar con 9 (ej. +51 9XXXXXXXX)")
    @Column(name = "telefono", length = 15, nullable = false)
    private String telefono;

    @Email(message = "Debe ser un correo electrónico válido")
    @Column(name = "correo", length = 150)
    private String correo;

    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;



    public Propietario() {
    }

    public Long getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paciente> pacientes = new ArrayList<>();
}