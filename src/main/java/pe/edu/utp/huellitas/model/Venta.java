package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Venta / Boleta generada por recepción.
 *
 * Una venta puede incluir servicios y/o productos del inventario.
 * Puede estar asociada a una cita previa (cita_id).
 *
 * Permisos:
 *   - Ver y crear: ADMINISTRADOR + RECEPCION
 *   - Anular: solo ADMINISTRADOR
 *
 * Flujo de estados: PENDIENTE → PAGADA | ANULADA
 */
@Data
@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número de boleta auto-generado. Formato: BOL-XXXXXXXX */
    @Column(unique = true, nullable = false, length = 20)
    private String nroBoleta;

    /** Propietario que paga. Puede ser null para clientes sin registro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id")
    private Propietario propietario;

    /** Recepcionista que registró la venta. */
    @NotNull(message = "El personal responsable es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    /** Cita de la cual se originó esta venta. Opcional. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Cita cita;

    /** Suma de subtotales antes de IGV. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** IGV aplicado. Por defecto 0 (muchas clínicas vet. no aplican IGV). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    /** Total a cobrar = subtotal + igv. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    /** Método de pago utilizado. */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", length = 20, nullable = false)
    private TipoPago tipoPago = TipoPago.EFECTIVO;

    /** Estado actual de la venta. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoVenta estado = EstadoVenta.PAGADA;

    @Column(nullable = false)
    private OffsetDateTime fechaEmision = OffsetDateTime.now();

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Líneas del detalle de la venta (productos y/o servicios). */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();
}