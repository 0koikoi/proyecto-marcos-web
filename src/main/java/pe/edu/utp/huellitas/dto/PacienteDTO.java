package pe.edu.utp.huellitas.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public class PacienteDTO {

    private Long id;

    @NotBlank(message = "El nombre del paciente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "La especie es obligatoria")
    private String especie;

    private String raza;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser en el futuro")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    private String genero;

    private String estado;
    
    private String alergias;
    
    private Boolean esterilizado;
    
    private Boolean fechaNacimientoEstimada;
    
    private java.math.BigDecimal pesoReferencia;

    @NotNull(message = "Debe seleccionar un propietario")
    private Long propietarioId;

    private String propietarioNombreCompleto;

    private String propietarioDni;

    public PacienteDTO() {
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

    public String getEstado() {
        return estado;
    }

    public String getAlergias() {
        return alergias;
    }

    public Boolean getEsterilizado() {
        return esterilizado;
    }

    public Boolean getFechaNacimientoEstimada() {
        return fechaNacimientoEstimada;
    }

    public java.math.BigDecimal getPesoReferencia() {
        return pesoReferencia;
    }

    public Long getPropietarioId() {
        return propietarioId;
    }

    public String getPropietarioNombreCompleto() {
        return propietarioNombreCompleto;
    }

    public String getPropietarioDni() {
        return propietarioDni;
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

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public void setEsterilizado(Boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public void setFechaNacimientoEstimada(Boolean fechaNacimientoEstimada) {
        this.fechaNacimientoEstimada = fechaNacimientoEstimada;
    }

    public void setPesoReferencia(java.math.BigDecimal pesoReferencia) {
        this.pesoReferencia = pesoReferencia;
    }

    public void setPropietarioId(Long propietarioId) {
        this.propietarioId = propietarioId;
    }

    public void setPropietarioNombreCompleto(String propietarioNombreCompleto) {
        this.propietarioNombreCompleto = propietarioNombreCompleto;
    }

    public void setPropietarioDni(String propietarioDni) {
        this.propietarioDni = propietarioDni;
    }
}