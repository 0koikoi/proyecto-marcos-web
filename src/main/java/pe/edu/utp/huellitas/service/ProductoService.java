package pe.edu.utp.huellitas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    // GUARDAR
    public Producto guardar(Producto producto) {

        //VALIDACIONES

        if (producto.getPrecioCompra() != null &&
                producto.getPrecioCompra().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El precio de compra no puede ser negativo");
        }

        if (producto.getPrecioVenta() != null &&
                producto.getPrecioVenta().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El precio de venta no puede ser negativo");
        }

        if (producto.getStockActual() != null &&
                producto.getStockActual() < 0) {
            throw new RuntimeException("El stock actual no puede ser negativo");
        }

        if (producto.getStockMinimo() != null &&
                producto.getStockMinimo() < 0) {
            throw new RuntimeException("El stock mínimo no puede ser negativo");
        }

        return productoRepository.save(producto);
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }
}