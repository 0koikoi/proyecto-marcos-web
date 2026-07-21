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
 *
 * V1 SQL columns: id, propietario_id, cita_id, personal_id, fecha,
 *                 total, tipo_pago, estado, motivo_anulacion,
 *                 anulado_por, anulado_en, creado_en, creado_por
 */
@Data
@Entity
@Table(name = "venta")
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Propietario que paga. Puede ser null para clientes sin registro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id")
    private Propietario propietario;

    /** Cita de la cual se originó esta venta. Opcional. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Cita cita;

    /** Recepcionista que registró la venta. */
    @NotNull(message = "El personal responsable es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    /** Fecha de emisión de la venta. */
    @Column(name = "fecha", nullable = false)
    private OffsetDateTime fecha = OffsetDateTime.now();

    /** Total a cobrar. */
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    /** Método de pago utilizado. */
    @Column(name = "tipo_pago", length = 20, nullable = false)
    private String tipoPago = "EFECTIVO";

    /** Estado actual de la venta. */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    /** Motivo de anulación (solo ADMINISTRADOR puede anular). */
    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    /** Personal que anuló la venta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por")
    private Personal anuladoPor;

    /** Fecha de anulación. */
    @Column(name = "anulado_en")
    private OffsetDateTime anuladoEn;

    // --- Auditoría ---

    @org.springframework.data.annotation.CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @org.springframework.data.annotation.CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", updatable = false)
    private Personal creadoPor;

    /** Líneas del detalle de la venta (productos y/o servicios). */
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();
}
