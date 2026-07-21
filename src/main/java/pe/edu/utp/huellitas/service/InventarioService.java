package pe.edu.utp.huellitas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.exception.NegocioException;
import pe.edu.utp.huellitas.model.MovimientoStock;
import pe.edu.utp.huellitas.model.OrigenMovimiento;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.model.TipoMovimiento;
import pe.edu.utp.huellitas.repository.MovimientoStockRepository;
import pe.edu.utp.huellitas.repository.ProductoRepository;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;

    @Transactional
    public void descontarStock(Long productoId, Integer cantidad, OrigenMovimiento origen, Long referenciaId) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a 0");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new NegocioException("Producto no encontrado con id: " + productoId));

        if (producto.getStockActual() < cantidad) {
            throw new NegocioException("Stock insuficiente en inventario para el producto " + producto.getNombre() + 
                    ". Disponible: " + producto.getStockActual() + ", Solicitado: " + cantidad);
        }

        producto.setStockActual(producto.getStockActual() - cantidad);
        productoRepository.save(producto);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProducto(producto);
        movimiento.setTipo(TipoMovimiento.SALIDA);
        movimiento.setCantidad(cantidad);
        movimiento.setOrigen(origen);
        movimiento.setReferenciaId(referenciaId);
        
        movimientoStockRepository.save(movimiento);
    }

    @Transactional
    public void incrementarStock(Long productoId, Integer cantidad, OrigenMovimiento origen, Long referenciaId) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a incrementar debe ser mayor a 0");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new NegocioException("Producto no encontrado con id: " + productoId));

        producto.setStockActual(producto.getStockActual() + cantidad);
        productoRepository.save(producto);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProducto(producto);
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(cantidad);
        movimiento.setOrigen(origen);
        movimiento.setReferenciaId(referenciaId);
        
        movimientoStockRepository.save(movimiento);
    }
}
