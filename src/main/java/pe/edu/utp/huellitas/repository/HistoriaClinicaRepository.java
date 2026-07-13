package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.utp.huellitas.model.HistoriaClinica;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {

    // El equipo puede agregar consultas personalizadas aquí, por ejemplo:
    List<HistoriaClinica> findByPacienteIdOrderByFechaConsultaDesc(Long pacienteId);
    List<HistoriaClinica> findByPersonalIdOrderByFechaConsultaDesc(Long personalId);

    /**
     * Busca historias clínicas filtrando por nombre de paciente o de propietario
     * (búsqueda parcial, insensible a mayúsculas) y por rango de fecha de consulta.
     * Cualquiera de los parámetros puede ser nulo para omitir ese filtro.
     */
    @Query("SELECT h FROM HistoriaClinica h WHERE " +
           "(LOWER(h.paciente.nombre) LIKE LOWER(CONCAT('%', COALESCE(:buscar, h.paciente.nombre), '%')) OR " +
           " LOWER(h.paciente.propietario.nombreCompleto) LIKE LOWER(CONCAT('%', COALESCE(:buscar, h.paciente.propietario.nombreCompleto), '%'))) " +
           "AND h.fechaConsulta >= COALESCE(:desde, h.fechaConsulta) " +
           "AND h.fechaConsulta <= COALESCE(:hasta, h.fechaConsulta) " +
           "ORDER BY h.fechaConsulta DESC")
    List<HistoriaClinica> buscar(@Param("buscar") String buscar,
                                  @Param("desde") OffsetDateTime desde,
                                  @Param("hasta") OffsetDateTime hasta);
}
