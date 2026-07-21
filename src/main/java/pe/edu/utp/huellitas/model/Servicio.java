package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Servicio veterinario ofrecido por la clínica (consulta, cirugía, baño, etc.)
 *
 * V1 SQL columns: id, nombre, descripcion, precio, activo,
 *                 creado_en, creado_por, actualizado_en, actualizado_por
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "servicio")
public class Servicio extends pe.edu.utp.huellitas.audit.Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;


}

