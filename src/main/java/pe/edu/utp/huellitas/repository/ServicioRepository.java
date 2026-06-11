package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.huellitas.model.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
}
