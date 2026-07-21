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

    /**
     * Verifica si ya existe una cita activa (no cancelada) para el mismo veterinario
     * en el mismo bloque de tiempo (ventana de ±duracionMinutos alrededor de fechaHora),
     * excluyendo opcionalmente la cita con el id dado (para edición).
     */
    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE " +
           "c.personal.id = :personalId AND " +
           "c.estado <> pe.edu.utp.huellitas.model.EstadoCita.CANCELADA AND " +
           "(:excludeId IS NULL OR c.id <> :excludeId) AND " +
           "c.fechaHora < :fin AND c.fechaHora > :inicio")
    boolean existeSolapamiento(@Param("personalId") Long personalId,
                               @Param("inicio") OffsetDateTime inicio,
                               @Param("fin") OffsetDateTime fin,
                               @Param("excludeId") Long excludeId);

    /**
     * Verifica si el paciente ya tiene una cita activa (no cancelada) en el mismo bloque de tiempo.
     */
    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE " +
           "c.paciente.id = :pacienteId AND " +
           "c.estado <> pe.edu.utp.huellitas.model.EstadoCita.CANCELADA AND " +
           "(:excludeId IS NULL OR c.id <> :excludeId) AND " +
           "c.fechaHora < :fin AND c.fechaHora > :inicio")
    boolean existeSolapamientoPaciente(@Param("pacienteId") Long pacienteId,
                                       @Param("inicio") OffsetDateTime inicio,
                                       @Param("fin") OffsetDateTime fin,
                                       @Param("excludeId") Long excludeId);
}
