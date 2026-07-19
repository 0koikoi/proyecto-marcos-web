package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Línea de detalle de una receta médica.
 * Cada fila representa un medicamento prescrito.
 *
 * V1 SQL columns: id, receta_id, medicamento, dosis, frecuencia, duracion, producto_id
 */
@Data
@Entity
@Table(name = "detalle_receta")
public class DetalleReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    @NotBlank(message = "El medicamento es obligatorio")
    @Column(name = "medicamento", nullable = false, length = 150)
    private String medicamento;

    /** Ej: "5 mg/kg", "1 comprimido" */
    @Column(name = "dosis", length = 100)
    private String dosis;

    /** Ej: "cada 8 horas", "una vez al día" */
    @Column(name = "frecuencia", length = 100)
    private String frecuencia;

    /** Ej: "7 días", "2 semanas". Almacenado como texto libre (VARCHAR). */
    @Column(name = "duracion", length = 100)
    private String duracion;

    /**
     * Enlace opcional al inventario. Solo cuando el medicamento es un producto
     * registrado — permite a Ventas precargarlo automáticamente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;
}
