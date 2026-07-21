package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.huellitas.exception.NegocioException;
import pe.edu.utp.huellitas.model.DetalleVenta;
import pe.edu.utp.huellitas.model.EstadoVenta;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.model.Venta;
import pe.edu.utp.huellitas.repository.DetalleVentaRepository;
import pe.edu.utp.huellitas.repository.ProductoRepository;
import pe.edu.utp.huellitas.repository.ServicioRepository;
import pe.edu.utp.huellitas.repository.VentaRepository;

import pe.edu.utp.huellitas.model.OrigenMovimiento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final InventarioService inventarioService;

    public VentaService(VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            ProductoRepository productoRepository,
            ServicioRepository servicioRepository,
            InventarioService inventarioService) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
        this.servicioRepository = servicioRepository;
        this.inventarioService = inventarioService;
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
                .orElseThrow(() -> new NegocioException("Venta no encontrada: " + id));
    }

    public List<DetalleVenta> obtenerDetallesPorVenta(Long ventaId) {
        return detalleVentaRepository.findByVentaId(ventaId);
    }

    @Transactional
    public void registrarVentaMultilinea(Venta venta, List<Long> itemIds, List<String> tiposItem, List<Integer> cantidades,
            String metodoPago) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new NegocioException("La venta debe tener al menos un producto o servicio.");
        }

        BigDecimal totalVenta = BigDecimal.ZERO;

        // 1. Guardar cabecera de la venta inicial
        venta.setFecha(OffsetDateTime.now());
        venta.setTipoPago(metodoPago != null ? metodoPago.toUpperCase() : "EFECTIVO");
        venta.setEstado(EstadoVenta.PENDIENTE);
        Venta ventaGuardada = ventaRepository.save(venta);

        // 2. Procesar detalles
        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            String tipo = tiposItem.get(i);
            Integer cantidad = cantidades.get(i);
            BigDecimal precioUnitario = BigDecimal.ZERO;
            
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(ventaGuardada);
            detalle.setCantidad(cantidad);

            if ("PRODUCTO".equalsIgnoreCase(tipo)) {
                Producto producto = productoRepository.findById(itemId)
                        .orElseThrow(() -> new NegocioException("Producto no encontrado: " + itemId));
                inventarioService.descontarStock(producto.getId(), cantidad, OrigenMovimiento.VENTA, ventaGuardada.getId());
                precioUnitario = producto.getPrecioVenta();
                detalle.setProducto(producto);
            } else if ("SERVICIO".equalsIgnoreCase(tipo)) {
                pe.edu.utp.huellitas.model.Servicio servicio = servicioRepository.findById(itemId)
                        .orElseThrow(() -> new NegocioException("Servicio no encontrado: " + itemId));
                precioUnitario = servicio.getPrecio();
                detalle.setServicio(servicio);
            } else {
                throw new NegocioException("Tipo de item inválido: " + tipo);
            }

            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
            totalVenta = totalVenta.add(subtotal);

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
            throw new NegocioException("La venta ya está anulada.");
        }

        // Revertir stock
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaId);
        for (DetalleVenta detalle : detalles) {
            if (detalle.getProducto() != null) {
                inventarioService.incrementarStock(detalle.getProducto().getId(), detalle.getCantidad(), OrigenMovimiento.VENTA, venta.getId());
            }
        }

        venta.setEstado(EstadoVenta.ANULADA);
        venta.setAnuladoEn(OffsetDateTime.now());
        ventaRepository.save(venta);
    }
}
