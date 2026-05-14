package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.huellitas.model.Producto;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    //@Query para el coptrol de la lógica de comparación de columnas
    @Query("SELECT p FROM Producto p WHERE p.stockActual <= p.stockMinimo")
    List<Producto> findProductosConStockCritico();
}