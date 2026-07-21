package pe.edu.utp.huellitas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.huellitas.model.Propietario;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {

    Optional<Propietario> findByDni(String dni);

    boolean existsByDni(String dni);

    java.util.List<Propietario> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDniContaining(
            String nombres, String apellidos, String dni);
}
