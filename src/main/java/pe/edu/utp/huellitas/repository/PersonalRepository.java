package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.huellitas.model.Personal;

public interface PersonalRepository extends JpaRepository<Personal, Long> {
}