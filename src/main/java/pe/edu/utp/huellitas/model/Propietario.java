package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "propietario")
public class Propietario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, length = 9)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Column(columnDefinition = "TEXT")
    private String direccion;
}