package pe.edu.utp.huellitas.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "especie", length = 50, nullable = false)
    private String especie;

    @Column(name = "raza", length = 50)
    private String raza;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 10)
    private String genero;

    @ManyToOne
    @JoinColumn(name = "propietario_id", nullable = false)
    private Propietario propietario;

    public Paciente() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public String getRaza() {
        return raza;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public Propietario getPropietario() {
        return propietario;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public void setPropietario(Propietario propietario) {
        this.propietario = propietario;
    }
}