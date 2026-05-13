package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "personal")
public class Personal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 7)
    private String codigoInstitucional;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, length = 50)
    private String cargo;

    @Column(length = 100)
    private String especialidad;

    @Column(length = 15)
    private String telefono;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean activo = true;
}