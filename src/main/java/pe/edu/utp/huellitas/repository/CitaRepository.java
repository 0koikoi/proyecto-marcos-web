package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.huellitas.model.Cita;

public interface CitaRepository extends JpaRepository<Cita, Long> {
}