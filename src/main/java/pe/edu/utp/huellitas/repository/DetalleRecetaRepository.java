package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.huellitas.model.DetalleReceta;

public interface DetalleRecetaRepository extends JpaRepository<DetalleReceta, Long> {
}
