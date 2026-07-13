package pe.edu.utp.huellitas.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.huellitas.model.Cita;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findTop5ByFechaHoraAfterOrderByFechaHoraAsc(OffsetDateTime fecha);

    List<Cita> findTop10ByOrderByFechaHoraDesc();

    List<Cita> findByFechaHoraBetweenOrderByFechaHoraAsc(
            OffsetDateTime inicio,
            OffsetDateTime fin
    );

}