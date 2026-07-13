package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.huellitas.model.Cita;

import java.time.OffsetDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    @Query("SELECT c FROM Cita c WHERE " +
           "(c.paciente.propietario.dni LIKE CONCAT('%', COALESCE(:dni, ''), '%')) AND " +
           "(CAST(:start AS timestamp) IS NULL OR c.fechaHora >= :start) AND " +
           "(CAST(:end AS timestamp) IS NULL OR c.fechaHora <= :end) " +
           "ORDER BY c.fechaHora DESC")
    List<Cita> buscarPorFiltros(@Param("dni") String dni, 
                                @Param("start") OffsetDateTime start, 
                                @Param("end") OffsetDateTime end);
}
