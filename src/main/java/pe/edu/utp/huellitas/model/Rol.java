package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Tabla de roles del sistema.
 * Valores válidos: ADMINISTRADOR, RECEPCION, VETERINARIO
 * Spring Security usará "ROLE_" + nombre (ej: ROLE_ADMINISTRADOR)
 */
@Data
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre; // ADMINISTRADOR | RECEPCION | VETERINARIO
}
