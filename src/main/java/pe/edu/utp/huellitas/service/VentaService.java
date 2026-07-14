package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.DetalleVenta;
import pe.edu.utp.huellitas.model.EstadoVenta;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.model.Venta;
import pe.edu.utp.huellitas.model.TipoPago;
import pe.edu.utp.huellitas.repository.DetalleVentaRepository;
import pe.edu.utp.huellitas.repository.ProductoRepository;
import pe.edu.utp.huellitas.repository.VentaRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    public List<Venta> listarVentas(EstadoVenta estado) {
        if (estado != null) {
            return ventaRepository.findByEstado(estado);
        }
        return ventaRepository.findAll();
    }

    public Venta obtenerPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
    }

    public List<DetalleVenta> obtenerDetallesPorVenta(Long ventaId) {
        return detalleVentaRepository.findByVentaId(ventaId);
    }

    @Transactional
    public void registrarVentaMultilinea(Venta venta, List<Long> productoIds, List<Integer> cantidades,
            String metodoPago) {
        if (productoIds == null || productoIds.isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un producto.");
        }

        BigDecimal totalVenta = BigDecimal.ZERO;

        // 1. Guardar cabecera de la venta inicial
        venta.setNroBoleta("BOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        venta.setFechaEmision(OffsetDateTime.now());
        venta.setTipoPago(TipoPago.valueOf(metodoPago.toUpperCase()));
        venta.setEstado(EstadoVenta.PENDIENTE); // Se crea como PENDIENTE para flujo de caja/entrega
        Venta ventaGuardada = ventaRepository.save(venta);

        // 2. Procesar detalles alineados a tabla_detalle.png
        for (int i = 0; i < productoIds.size(); i++) {
            Long productoId = productoIds.get(i);
            Integer cantidad = cantidades.get(i);

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

            if (producto.getStockActual() < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre());
            }

            // Descontar stock
            producto.setStockActual(producto.getStockActual() - cantidad);
            productoRepository.save(producto);

            // Cálculos
            BigDecimal precioUnitario = producto.getPrecioVenta();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
            totalVenta = totalVenta.add(subtotal);

            // Persistir detalle (Esquema detalle_venta)
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(ventaGuardada);
            detalle.setProducto(producto);
            // servicio_id y descripcion quedan nulos según el constraint
            // ck_detalle_tiene_item si hay producto
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);

            detalleVentaRepository.save(detalle);
        }

        // 3. Actualizar totales de la cabecera
        ventaGuardada.setSubtotal(totalVenta);
        ventaGuardada.setIgv(BigDecimal.ZERO); // Aplicar lógica de IGV si es necesario
        ventaGuardada.setTotal(totalVenta);
        ventaRepository.save(ventaGuardada);
    }

    @Transactional
    public void anularVenta(Long ventaId) {
        Venta venta = obtenerPorId(ventaId);

        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden anular ventas en estado PENDIENTE.");
        }

        // Revertir Stock
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaId);
        for (DetalleVenta detalle : detalles) {
            if (detalle.getProducto() != null) {
                Producto producto = detalle.getProducto();
                producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        venta.setEstado(EstadoVenta.ANULADA);
        ventaRepository.save(venta);
    }
}
