package pe.edu.utp.huellitas.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pe.edu.utp.huellitas.model.HistoriaClinica;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {

    /** Historial de un paciente, más reciente primero. Campo renombrado: fecha */
    List<HistoriaClinica> findByPacienteIdOrderByFechaDesc(Long pacienteId);

    /** Historias atendidas por un veterinario específico. */
    List<HistoriaClinica> findByPersonalIdOrderByFechaDesc(Long personalId);

    /**
     * Busca historias clínicas filtrando por nombre de paciente o propietario y por rango de fechas.
     * Todos los parámetros son opcionales (null = sin filtro).
     * Nota: propietario ahora usa nombres+apellidos separados.
     */
    @Query("SELECT h FROM HistoriaClinica h WHERE " +
           "(LOWER(h.paciente.nombre) LIKE LOWER(CONCAT('%', COALESCE(:buscar, h.paciente.nombre), '%')) OR " +
           " LOWER(h.paciente.propietario.nombres) LIKE LOWER(CONCAT('%', COALESCE(:buscar, h.paciente.propietario.nombres), '%')) OR " +
           " LOWER(h.paciente.propietario.apellidos) LIKE LOWER(CONCAT('%', COALESCE(:buscar, h.paciente.propietario.apellidos), '%'))) " +
           "AND h.fecha >= COALESCE(:desde, h.fecha) " +
           "AND h.fecha <= COALESCE(:hasta, h.fecha) " +
           "ORDER BY h.fecha DESC")
    List<HistoriaClinica> buscar(@Param("buscar") String buscar,
                                  @Param("desde") OffsetDateTime desde,
                                  @Param("hasta") OffsetDateTime hasta);
                                  
    boolean existsByPacienteId(Long pacienteId);
    boolean existsByCitaId(Long citaId);
}
