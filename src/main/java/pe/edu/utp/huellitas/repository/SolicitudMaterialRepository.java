package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.huellitas.model.EstadoSolicitud;
import pe.edu.utp.huellitas.model.SolicitudMaterial;

import java.util.List;

@Repository
public interface SolicitudMaterialRepository extends JpaRepository<SolicitudMaterial, Long> {

    /** Filtra por estado (enum). Usado para mostrar pendientes al administrador. */
    List<SolicitudMaterial> findByEstadoOrderByCreatedAtDesc(EstadoSolicitud estado);

    /** Lista todas las solicitudes de un veterinario específico. */
    List<SolicitudMaterial> findBySolicitanteIdOrderByCreatedAtDesc(Long personalId);

    /** Lista todas las solicitudes, ordenadas por fecha descendente. */
    List<SolicitudMaterial> findAllByOrderByCreatedAtDesc();
}
