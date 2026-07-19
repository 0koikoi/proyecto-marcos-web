package pe.edu.utp.huellitas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.huellitas.model.EstadoSolicitud;
import pe.edu.utp.huellitas.model.SolicitudMaterial;

import java.util.List;

@Repository
public interface SolicitudMaterialRepository extends JpaRepository<SolicitudMaterial, Long> {

    /** Filtra por estado. Usado para mostrar pendientes al administrador. */
    List<SolicitudMaterial> findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud estado);

    /** Lista todas las solicitudes de un veterinario específico. */
    List<SolicitudMaterial> findBySolicitanteIdOrderByFechaSolicitudDesc(Long personalId);

    /** Lista todas las solicitudes, más reciente primero. */
    List<SolicitudMaterial> findAllByOrderByFechaSolicitudDesc();
}
