package pe.edu.utp.huellitas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.huellitas.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findByPropietarioId(Long propietarioId);

    List<Paciente> findByNombreContainingIgnoreCase(String nombre);

    List<Paciente> findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCaseOrPropietarioNombresContainingIgnoreCaseOrPropietarioApellidosContainingIgnoreCase(
            String nombre, String especie, String propietarioNombres, String propietarioApellidos);

    List<Paciente> findTop10ByOrderByCreadoEnDesc();

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Paciente p WHERE p.propietario.id = :propietarioId AND LOWER(p.nombre) = LOWER(:nombre) AND LOWER(p.especie) = LOWER(:especie)")
    List<Paciente> buscarSimilares(@org.springframework.data.repository.query.Param("propietarioId") Long propietarioId, @org.springframework.data.repository.query.Param("nombre") String nombre, @org.springframework.data.repository.query.Param("especie") String especie);
}