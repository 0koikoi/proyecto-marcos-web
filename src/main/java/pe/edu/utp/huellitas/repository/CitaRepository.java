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

  @Query("""
    SELECT c
    FROM Cita c
    WHERE
        (:dni IS NULL OR c.paciente.propietario.dni LIKE CONCAT('%', :dni, '%'))
        AND (c.fechaHora >= COALESCE(:start, c.fechaHora))
        AND (c.fechaHora <= COALESCE(:end, c.fechaHora))
    ORDER BY c.fechaHora DESC
""")
List<Cita> buscarPorFiltros(
        @Param("dni") String dni,
        @Param("start") OffsetDateTime start,
        @Param("end") OffsetDateTime end
);

    // Citas activas del veterinario
    @Query("""
        SELECT c
        FROM Cita c
        WHERE c.personal.id = :personalId
        AND c.estado <> pe.edu.utp.huellitas.model.EstadoCita.CANCELADA
    """)
    List<Cita> buscarCitasDelVeterinario(
            @Param("personalId") Long personalId
    );

    // Citas activas del paciente
    @Query("""
        SELECT c
        FROM Cita c
        WHERE c.paciente.id = :pacienteId
        AND c.estado <> pe.edu.utp.huellitas.model.EstadoCita.CANCELADA
    """)
    List<Cita> buscarCitasDelPaciente(
            @Param("pacienteId") Long pacienteId
    );

    // Verificar si un paciente tiene citas
    boolean existsByPacienteId(Long pacienteId);

}