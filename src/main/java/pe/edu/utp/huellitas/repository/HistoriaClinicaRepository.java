package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.huellitas.model.HistoriaClinica;

import java.util.List;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {
    
    // El equipo puede agregar consultas personalizadas aquí, por ejemplo:
    List<HistoriaClinica> findByPacienteIdOrderByFechaConsultaDesc(Long pacienteId);
    List<HistoriaClinica> findByPersonalIdOrderByFechaConsultaDesc(Long personalId);
}
