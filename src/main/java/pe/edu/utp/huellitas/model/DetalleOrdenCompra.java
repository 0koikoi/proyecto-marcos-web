package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalle_orden_compra")
public class DetalleOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrdenCompra ordenCompra;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "La cantidad solicitada es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    @Min(value = 0, message = "La cantidad recibida no puede ser negativa")
    @Column(name = "cantidad_recibida")
    private Integer cantidadRecibida;

    @NotNull(message = "El precio unitario es obligatorio")
    @Min(value = 0, message = "El precio unitario no puede ser negativo")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
}
