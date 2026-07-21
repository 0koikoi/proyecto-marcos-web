package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.EstadoSolicitud;
import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.model.SolicitudMaterial;
import pe.edu.utp.huellitas.repository.PersonalRepository;
import pe.edu.utp.huellitas.repository.ProductoRepository;
import pe.edu.utp.huellitas.repository.SolicitudMaterialRepository;

import pe.edu.utp.huellitas.model.OrigenMovimiento;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Servicio de gestión de solicitudes de material e insumos.
 *
 * TODO — La estructura base está lista. El desarrollador asignado (Persona 3) debe:
 *   1. Crear SolicitudMaterialController en package controller/
 *   2. Crear las vistas Thymeleaf en templates/solicitudes/
 *   3. Al marcar como ENTREGADA, marcarEntregada() ya descuenta el stock.
 */
@Service
public class SolicitudMaterialService {

    private final SolicitudMaterialRepository solicitudRepository;
    private final PersonalRepository personalRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;

    public SolicitudMaterialService(SolicitudMaterialRepository solicitudRepository,
                                    PersonalRepository personalRepository,
                                    ProductoRepository productoRepository,
                                    InventarioService inventarioService) {
        this.solicitudRepository = solicitudRepository;
        this.personalRepository = personalRepository;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /** Lista todas las solicitudes (para ADMINISTRADOR). */
    public List<SolicitudMaterial> listarTodas() {
        return solicitudRepository.findAllByOrderByFechaSolicitudDesc();
    }

    /** Lista todas las solicitudes de un veterinario (para vista del VET). */
    public List<SolicitudMaterial> listarPorSolicitante(Long personalId) {
        return solicitudRepository.findBySolicitanteIdOrderByFechaSolicitudDesc(personalId);
    }

    /** Lista solicitudes en estado PENDIENTE (para el panel del administrador). */
    public List<SolicitudMaterial> listarPendientes() {
        return solicitudRepository.findByEstadoOrderByFechaSolicitudDesc(EstadoSolicitud.PENDIENTE);
    }

    public SolicitudMaterial obtenerPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la solicitud con ID: " + id));
    }

    // ── Crear solicitud (VETERINARIO) ─────────────────────────────────────────

    @Transactional
    public SolicitudMaterial guardar(SolicitudMaterial solicitud) {
        if (solicitud.getSolicitante() == null) {
            throw new IllegalArgumentException("El solicitante es obligatorio.");
        }
        if (solicitud.getProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        if (solicitud.getCantidad() == null || solicitud.getCantidad() < 1) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (solicitud.getMotivo() == null || solicitud.getMotivo().isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio.");
        }
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitudRepository.save(solicitud);
    }

    // ── Aprobar (ADMINISTRADOR) ───────────────────────────────────────────────

    @Transactional
    public void aprobar(Long solicitudId, Long adminId) {
        SolicitudMaterial solicitud = obtenerPorId(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden aprobar solicitudes en estado PENDIENTE. " +
                    "Estado actual: " + solicitud.getEstado());
        }

        Personal admin = personalRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado."));

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setPersonalRespuesta(admin);
        solicitud.setFechaRespuesta(OffsetDateTime.now());
        solicitudRepository.save(solicitud);
    }

    // ── Rechazar (ADMINISTRADOR) ──────────────────────────────────────────────

    @Transactional
    public void rechazar(Long solicitudId, Long adminId) {
        SolicitudMaterial solicitud = obtenerPorId(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden rechazar solicitudes en estado PENDIENTE.");
        }

        Personal admin = personalRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado."));

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setPersonalRespuesta(admin);
        solicitud.setFechaRespuesta(OffsetDateTime.now());
        solicitudRepository.save(solicitud);
    }

    // ── Marcar como entregada (ADMINISTRADOR) ─────────────────────────────────

    /**
     * Marca la solicitud como entregada y descuenta el stock del producto.
     * TODO (Fase 3): mover descuento de stock a InventarioService.descontarStock()
     */
    @Transactional
    public void marcarEntregada(Long solicitudId) {
        SolicitudMaterial solicitud = obtenerPorId(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.APROBADA) {
            throw new IllegalStateException(
                    "Solo se pueden marcar como entregadas solicitudes en estado APROBADA.");
        }

        // Descontar stock del inventario
        Producto producto = solicitud.getProducto();
        inventarioService.descontarStock(producto.getId(), solicitud.getCantidad(), OrigenMovimiento.SOLICITUD, solicitud.getId());

        solicitud.setEstado(EstadoSolicitud.ENTREGADA);
        solicitud.setFechaRespuesta(OffsetDateTime.now());
        solicitudRepository.save(solicitud);
    }
}
