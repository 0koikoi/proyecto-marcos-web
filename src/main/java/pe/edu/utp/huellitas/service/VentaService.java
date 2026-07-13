package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.DetalleVenta;
import pe.edu.utp.huellitas.model.EstadoVenta;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.model.Venta;
import pe.edu.utp.huellitas.repository.DetalleVentaRepository;
import pe.edu.utp.huellitas.repository.ProductoRepository;
import pe.edu.utp.huellitas.repository.VentaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de gestión de ventas y facturación.
 *
 * Permisos esperados en el controller:
 *   - Ver y crear ventas: ADMINISTRADOR + RECEPCION
 *   - Anular ventas:      solo ADMINISTRADOR
 *
 * TODO para el equipo (módulo de ventas completo):
 *   - Implementar registrarVentaConDetalle(Venta, List<DetalleVentaRequest>)
 *     para soportar múltiples productos y servicios en una sola venta.
 *   - Implementar anular(Long ventaId) que revierta el stock.
 *   - Implementar reporteVentas(LocalDate desde, LocalDate hasta) para el dashboard.
 */
@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;

    public VentaService(VentaRepository ventaRepository,
                        DetalleVentaRepository detalleVentaRepository,
                        ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    // ── Registrar venta (1 producto) ──────────────────────────────────────────

    /**
     * Registra una venta con un solo producto.
     *
     * Flujo:
     *   1. Valida existencia del producto y stock suficiente.
     *   2. Descuenta el stock del inventario.
     *   3. Calcula subtotal e IGV (0% por defecto — ajustar si aplica).
     *   4. Guarda la venta con número de boleta auto-generado.
     *   5. Guarda el detalle de la línea.
     *
     * NOTA: Este método solo soporta 1 producto por venta.
     * El módulo completo de ventas debe usar registrarVentaConDetalle().
     *
     * @param venta      Objeto Venta con propietario y personal ya asignados
     * @param productoId ID del producto a vender
     * @param cantidad   Cantidad a vender
     * @throws IllegalArgumentException si el producto no existe o no hay stock
     */
    @Transactional
    public void registrarVenta(Venta venta, Long productoId, Integer cantidad) {

        // 1. Validar Producto y Stock
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado con ID: " + productoId));

        if (producto.getStockActual() < cantidad) {
            throw new IllegalArgumentException(
                    "Stock insuficiente para: " + producto.getNombre() +
                    ". Disponible: " + producto.getStockActual() +
                    ", Solicitado: " + cantidad);
        }

        // 2. Descontar Stock del Inventario
        producto.setStockActual(producto.getStockActual() - cantidad);
        productoRepository.save(producto);

        // 3. Calcular totales
        BigDecimal subtotal = producto.getPrecioVenta().multiply(BigDecimal.valueOf(cantidad));
        BigDecimal igv = BigDecimal.ZERO;         // Ajustar a 0.18 si aplica IGV
        BigDecimal total = subtotal.add(igv);

        // 4. Preparar y Guardar la Venta
        venta.setNroBoleta("BOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        venta.setSubtotal(subtotal);
        venta.setIgv(igv);
        venta.setTotal(total);
        venta.setEstado(EstadoVenta.PAGADA);
        ventaRepository.save(venta);

        // 5. Crear y Guardar el Detalle
        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecioVenta());
        detalle.setSubtotal(subtotal);
        detalleVentaRepository.save(detalle);
    }
}
