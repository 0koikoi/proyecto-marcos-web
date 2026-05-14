package pe.edu.utp.huellitas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "propietario")
public class Propietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dni", length = 8, nullable = false, unique = true)
    private String dni;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "telefono", length = 9, nullable = false)
    private String telefono;

    @Column(name = "correo", length = 150)
    private String correo;

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

    public String getNombreCompleto() {
        return nombreCompleto;
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

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
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
}