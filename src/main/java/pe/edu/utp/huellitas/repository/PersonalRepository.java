package pe.edu.utp.huellitas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utp.huellitas.model.Personal;

public interface PersonalRepository extends JpaRepository<Personal, Long> {

    Optional<Personal> findByUsername(String username);

    List<Personal> findByRolNombre(String nombreRol);
}