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
import java.time.OffsetDateTime;
import java.util.List;

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
        venta.setFecha(OffsetDateTime.now());
        venta.setTipoPago(metodoPago != null ? metodoPago.toUpperCase() : "EFECTIVO");
        venta.setEstado(EstadoVenta.PENDIENTE);
        Venta ventaGuardada = ventaRepository.save(venta);

        // 2. Procesar detalles
        for (int i = 0; i < productoIds.size(); i++) {
            Long productoId = productoIds.get(i);
            Integer cantidad = cantidades.get(i);

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

            if (producto.getStockActual() < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre());
            }

            // Descontar stock
            // TODO (Fase 3): mover a InventarioService.descontarStock()
            producto.setStockActual(producto.getStockActual() - cantidad);
            productoRepository.save(producto);

            BigDecimal precioUnitario = producto.getPrecioVenta();
            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
            totalVenta = totalVenta.add(subtotal);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(ventaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            detalleVentaRepository.save(detalle);
        }

        // 3. Actualizar total de la cabecera
        ventaGuardada.setTotal(totalVenta);
        ventaRepository.save(ventaGuardada);
    }

    @Transactional
    public void anularVenta(Long ventaId) {
        Venta venta = obtenerPorId(ventaId);

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new IllegalStateException("La venta ya está anulada.");
        }

        // Revertir stock
        // TODO (Fase 3): mover a InventarioService.incrementarStock()
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaId);
        for (DetalleVenta detalle : detalles) {
            if (detalle.getProducto() != null) {
                Producto producto = detalle.getProducto();
                producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        venta.setEstado(EstadoVenta.ANULADA);
        venta.setAnuladoEn(OffsetDateTime.now());
        ventaRepository.save(venta);
    }
}
