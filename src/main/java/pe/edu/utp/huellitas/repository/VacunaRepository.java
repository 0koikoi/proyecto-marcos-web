package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.huellitas.model.Vacuna;

import java.time.LocalDate;
import java.util.List;

public interface VacunaRepository extends JpaRepository<Vacuna, Long> {

    List<Vacuna> findByPacienteIdOrderByFechaAplicacionDesc(Long pacienteId);

    // Útil para alertas en el dashboard
    List<Vacuna> findByFechaProximaDosisBetween(LocalDate inicio, LocalDate fin);
}
