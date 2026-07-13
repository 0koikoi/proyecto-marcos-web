package pe.edu.utp.huellitas.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import pe.edu.utp.huellitas.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("""
        SELECT COALESCE(SUM(v.total),0)
        FROM Venta v
        WHERE MONTH(v.fechaEmision)=MONTH(CURRENT_DATE)
        AND YEAR(v.fechaEmision)=YEAR(CURRENT_DATE)
    """)
    BigDecimal totalVentasMes();

    List<Venta> findTop10ByOrderByFechaEmisionDesc();

}