package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.huellitas.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}