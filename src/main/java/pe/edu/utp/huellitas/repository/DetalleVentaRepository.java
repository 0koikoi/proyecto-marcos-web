package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.huellitas.model.DetalleVenta;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaId(Long ventaId);

    @Query("""
        SELECT d.producto.nombre, SUM(d.cantidad)
        FROM DetalleVenta d
        WHERE d.producto IS NOT NULL
        GROUP BY d.producto.nombre
        ORDER BY SUM(d.cantidad) DESC
    """)
    List<Object[]> productosMasVendidos();

}
