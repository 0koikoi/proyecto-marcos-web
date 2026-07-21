package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.huellitas.exception.NegocioException;
import pe.edu.utp.huellitas.model.DetalleVenta;
import pe.edu.utp.huellitas.model.EstadoVenta;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.model.Servicio;
import pe.edu.utp.huellitas.model.Venta;
import pe.edu.utp.huellitas.repository.DetalleVentaRepository;
import pe.edu.utp.huellitas.repository.ProductoRepository;
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
    private final InventarioService inventarioService;
    private final ServicioService servicioService;

    public VentaService(VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            ProductoRepository productoRepository,
            InventarioService inventarioService,
            ServicioService servicioService) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
        this.servicioService = servicioService;
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

    /**
     * Registra una venta con líneas mixtas: cada línea es un producto del inventario
     * O un servicio veterinario (nunca ambos), alineados por índice entre los tres arrays.
     *
     * @param itemIds   ID de producto o de servicio según corresponda a esa línea
     * @param itemTipos "PRODUCTO" o "SERVICIO" para cada línea (mismo índice que itemIds)
     * @param cantidades cantidad de esa línea (mismo índice)
     * @param metodoPago EFECTIVO / TARJETA / TRANSFERENCIA
     */
    @Transactional
    public void registrarVentaMultilinea(Venta venta, List<Long> itemIds, List<String> itemTipos,
            List<Integer> cantidades, String metodoPago) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new NegocioException("La venta debe tener al menos una línea (producto o servicio).");
        }
        if (itemTipos == null || itemTipos.size() != itemIds.size() || cantidades.size() != itemIds.size()) {
            throw new NegocioException("Las líneas de la venta están incompletas o mal formadas.");

        BigDecimal totalVenta = BigDecimal.ZERO;

        // 1. Guardar cabecera de la venta inicial
        venta.setFecha(OffsetDateTime.now());
        venta.setTipoPago(metodoPago != null ? metodoPago.toUpperCase() : "EFECTIVO");
        venta.setEstado(EstadoVenta.PENDIENTE);
        Venta ventaGuardada = ventaRepository.save(venta);

        // 2. Procesar detalles: cada línea es producto (descuenta stock vía InventarioService,
        //    nunca tocando Producto directamente) o servicio (no mueve stock).
        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            Integer cantidad = cantidades.get(i);
            String tipo = itemTipos.get(i) != null ? itemTipos.get(i).toUpperCase() : "";

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(ventaGuardada);
            detalle.setCantidad(cantidad);

            BigDecimal precioUnitario;

            if ("SERVICIO".equals(tipo)) {
                Servicio servicio = servicioService.buscarPorId(itemId);
                if (servicio == null) {
                    throw new NegocioException("Servicio no encontrado: " + itemId);
                }
                if (Boolean.FALSE.equals(servicio.getActivo())) {
                    throw new NegocioException("El servicio '" + servicio.getNombre() + "' no está activo.");
                }
                precioUnitario = servicio.getPrecio();
                detalle.setServicio(servicio);
            } else if ("PRODUCTO".equals(tipo)) {
                Producto producto = productoRepository.findById(itemId)
                        .orElseThrow(() -> new NegocioException("Producto no encontrado: " + itemId));

                inventarioService.descontarStock(producto.getId(), cantidad, OrigenMovimiento.VENTA, ventaGuardada.getId());

                precioUnitario = producto.getPrecioVenta();
                detalle.setProducto(producto);
            } else {
                throw new NegocioException("Tipo de línea inválido: '" + tipo + "' (debe ser PRODUCTO o SERVICIO).");
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
