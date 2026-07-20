package pe.edu.utp.huellitas.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.huellitas.model.Cita;
import pe.edu.utp.huellitas.model.EstadoCita;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findTop5ByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(
        EstadoCita estado,
        OffsetDateTime fechaHora
    );

    List<Cita> findTop10ByOrderByFechaHoraDesc();

    List<Cita> findByFechaHoraBetweenOrderByFechaHoraAsc(
            OffsetDateTime inicio,
            OffsetDateTime fin
    );

    @Query("SELECT c FROM Cita c WHERE " +
           "(c.paciente.propietario.dni LIKE CONCAT('%', COALESCE(:dni, ''), '%')) AND " +
           "(CAST(:start AS timestamp) IS NULL OR c.fechaHora >= :start) AND " +
           "(CAST(:end AS timestamp) IS NULL OR c.fechaHora <= :end) " +
           "ORDER BY c.fechaHora DESC")
    List<Cita> buscarPorFiltros(@Param("dni") String dni, 
                                @Param("start") OffsetDateTime start, 
                                @Param("end") OffsetDateTime end);
                                
    boolean existsByPacienteId(Long pacienteId);
}
