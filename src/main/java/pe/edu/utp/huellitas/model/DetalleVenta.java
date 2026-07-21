package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Línea de detalle de una venta.
 * Cada fila representa un producto O un servicio (nunca ambos ni ninguno).
 *
 * V1 SQL columns: id, venta_id, producto_id, servicio_id,
 *                 cantidad, precio_unitario, subtotal
 * CHECK: (producto_id IS NOT NULL AND servicio_id IS NULL) OR
 *        (producto_id IS NULL AND servicio_id IS NOT NULL)
 */
@Data
@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    /** Puede ser nulo si están cobrando un servicio. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    /** Puede ser nulo si están cobrando un producto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}
