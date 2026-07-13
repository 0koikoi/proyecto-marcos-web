package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.huellitas.model.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Productos con stock en nivel crítico (stock_actual <= stock_minimo).
     * Usado en el dashboard para mostrar alertas.
     */
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo AND p.activo = true")
    List<Producto> findStockCritico();

    /**
     * Alias mantenido por compatibilidad con código existente.
     * @deprecated Usar {@link #findStockCritico()} en código nuevo.
     */
    @Deprecated
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo")
    List<Producto> findProductosConStockCritico();
}


