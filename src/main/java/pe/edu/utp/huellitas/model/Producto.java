package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Producto del inventario de la clínica.
 *
 * V1 SQL columns: id, nombre, descripcion, categoria, precio_venta,
 *                 stock_actual, stock_minimo, activo, version,
 *                 creado_en, creado_por, actualizado_en, actualizado_por
 *
 * NOTA: precio_compra y proveedor_id NO existen en la tabla producto del V1.
 *       El costo de compra se registra en detalle_orden_compra.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "producto")
public class Producto extends pe.edu.utp.huellitas.audit.Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @NotNull(message = "El precio de venta es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Versión para optimistic locking.
     * Si dos operaciones intentan modificar el mismo stock al mismo tiempo,
     * la segunda falla de forma controlada (OptimisticLockException).
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}

