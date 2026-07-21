package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.huellitas.model.DetalleOrdenCompra;

public interface DetalleOrdenCompraRepository extends JpaRepository<DetalleOrdenCompra, Long> {
}
