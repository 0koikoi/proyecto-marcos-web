package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.DetalleVenta;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.model.Venta;
import pe.edu.utp.huellitas.repository.DetalleVentaRepository;
import pe.edu.utp.huellitas.repository.ProductoRepository;
import pe.edu.utp.huellitas.repository.VentaRepository;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;

    public VentaService(VentaRepository ventaRepository, DetalleVentaRepository detalleVentaRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
    }

    public Iterable<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Transactional // CRÍTICO: Si algo falla aquí, la base de datos retrocede todo.
    public void registrarVenta(Venta venta, Long productoId, Integer cantidad) {
        
        // 1. Validar Producto y Stock
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (producto.getStockActual() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para: " + producto.getNombre());
        }

        // 2. Descontar Stock del Inventario
        producto.setStockActual(producto.getStockActual() - cantidad);
        productoRepository.save(producto);

        // 3. Preparar y Guardar la Venta
        venta.setNroBoleta("BOL-" + UUID.randomWindow().toString().substring(0, 8).toUpperCase());
        BigDecimal subtotal = producto.getPrecioVenta().multiply(new BigDecimal(cantidad));
        venta.setTotal(subtotal);
        ventaRepository.save(venta);

        // 4. Crear y Guardar el Detalle
        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecioVenta());
        detalle.setSubtotal(subtotal);
        detalleVentaRepository.save(detalle);
    }
}
