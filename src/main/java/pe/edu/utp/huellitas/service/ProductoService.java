package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de gestión del inventario de productos y medicamentos.
 *
 * Permisos esperados en el controller:
 *   - Leer (listar, buscar): todos los roles autenticados
 *   - Guardar / Eliminar:    solo ADMINISTRADOR
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    /**
     * Lista los productos cuyo stock actual está en nivel crítico
     * (stock_actual <= stock_minimo). Usado en el dashboard.
     */
    public List<Producto> listarStockCritico() {
        return productoRepository.findStockCritico();
    }

    /**
     * Busca un producto por su ID.
     * @throws IllegalArgumentException si el producto no existe.
     */
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el producto con ID: " + id));
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    /**
     * Guarda o actualiza un producto del inventario.
     * Valida que los precios y stocks no sean negativos.
     *
     * @param producto Entidad con los datos a persistir
     * @return El producto guardado con su ID asignado
     * @throws IllegalArgumentException si algún valor es inválido
     */
    @Transactional
    public Producto guardar(Producto producto) {
        validarProducto(producto);
        return productoRepository.save(producto);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    /**
     * Elimina un producto por su ID.
     * Advertencia: verificar que no tenga ventas asociadas antes de eliminar.
     */
    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el producto con ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    // ── Validación privada ────────────────────────────────────────────────────

    private void validarProducto(Producto producto) {
        if (producto.getPrecioCompra() != null &&
                producto.getPrecioCompra().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de compra no puede ser negativo");
        }
        if (producto.getPrecioVenta() != null &&
                producto.getPrecioVenta().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser negativo");
        }
        if (producto.getStockActual() != null &&
                producto.getStockActual() < 0) {
            throw new IllegalArgumentException("El stock actual no puede ser negativo");
        }
        if (producto.getStockMinimo() != null &&
                producto.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
    }
}
