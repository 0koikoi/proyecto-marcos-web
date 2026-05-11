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
    private String codigoInstitucional; // Ej: C123456

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String cargo;
}