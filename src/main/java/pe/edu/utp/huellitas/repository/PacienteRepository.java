package pe.edu.utp.huellitas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.huellitas.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findByPropietarioId(Long propietarioId);

    List<Paciente> findByNombreContainingIgnoreCase(String nombre);

    List<Paciente> findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCaseOrPropietarioNombreCompletoContainingIgnoreCase(
            String nombre, String especie, String propietario);

    List<Paciente> findTop10ByOrderByCreatedAtDesc();
}