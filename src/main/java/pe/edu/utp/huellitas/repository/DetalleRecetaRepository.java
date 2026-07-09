package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.huellitas.model.DetalleReceta;

@Repository
public interface DetalleRecetaRepository extends JpaRepository<DetalleReceta, Long> {
}
