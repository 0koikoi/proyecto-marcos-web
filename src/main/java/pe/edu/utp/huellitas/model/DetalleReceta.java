package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Línea de detalle de una receta médica.
 * Cada fila representa un medicamento prescrito.
 */
@Data
@Entity
@Table(name = "detalle_receta")
public class DetalleReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    @NotBlank(message = "El medicamento es obligatorio")
    @Column(nullable = false, length = 150)
    private String medicamento;

    /** Ej: comprimido, jarabe, inyectable, tópico */
    @Column(length = 50)
    private String presentacion;

    /** Ej: "5 mg/kg", "1 comprimido" */
    @NotBlank(message = "La dosis es obligatoria")
    @Column(nullable = false, length = 100)
    private String dosis;

    /** Ej: "cada 8 horas", "una vez al día" */
    @NotBlank(message = "La frecuencia es obligatoria")
    @Column(nullable = false, length = 100)
    private String frecuencia;

    @Min(value = 1, message = "La duración debe ser al menos 1 día")
    private Integer duracionDias;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
