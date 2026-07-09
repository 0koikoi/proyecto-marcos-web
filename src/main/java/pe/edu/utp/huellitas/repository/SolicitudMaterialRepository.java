package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.huellitas.model.SolicitudMaterial;

import java.util.List;

@Repository
public interface SolicitudMaterialRepository extends JpaRepository<SolicitudMaterial, Long> {

    List<SolicitudMaterial> findByEstado(String estado);
    List<SolicitudMaterial> findBySolicitanteId(Long personalId);
}
