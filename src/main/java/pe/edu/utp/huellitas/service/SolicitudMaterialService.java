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

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Servicio de gestión de solicitudes de material e insumos.
 *
 * ════════════════════════════════════════════════════════════
 * TODO — MÓDULO A IMPLEMENTAR POR EL EQUIPO
 * ════════════════════════════════════════════════════════════
 *
 * La estructura base está lista. El desarrollador asignado debe:
 *
 *   1. Crear SolicitudMaterialController en package controller/
 *
 *   2. Crear las vistas Thymeleaf en templates/solicitudes/:
 *      - lista.html      → vista diferenciada por rol:
 *                          ADMIN ve todas, VET ve solo las suyas
 *      - formulario.html → veterinario crea nueva solicitud
 *
 *   3. Implementar en el controller los botones de acción del ADMIN:
 *      - Aprobar (POST /solicitudes/aprobar/{id})
 *      - Rechazar (POST /solicitudes/rechazar/{id})  ← requiere observación
 *      - Marcar como entregada (POST /solicitudes/entregar/{id})
 *
 *   4. Al marcar como ENTREGADA, el método marcarEntregada() ya descuenta
 *      el stock del producto. Verificar que el stock no quede negativo.
 *
 *   5. Agregar @PreAuthorize granular por método en el controller.
 *
 * Rutas esperadas del controller:
 *   GET  /solicitudes                → lista (filtrada por rol automáticamente)
 *   GET  /solicitudes/nueva          → formulario (solo VET)
 *   POST /solicitudes/guardar        → crear (solo VET)
 *   POST /solicitudes/aprobar/{id}   → aprobar (solo ADMIN)
 *   POST /solicitudes/rechazar/{id}  → rechazar (solo ADMIN)
 *   POST /solicitudes/entregar/{id}  → marcar entregada (solo ADMIN)
 * ════════════════════════════════════════════════════════════
 */
@Service
public class SolicitudMaterialService {

    private final SolicitudMaterialRepository solicitudRepository;
    private final PersonalRepository personalRepository;
    private final ProductoRepository productoRepository;

    public SolicitudMaterialService(SolicitudMaterialRepository solicitudRepository,
                                    PersonalRepository personalRepository,
                                    ProductoRepository productoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.personalRepository = personalRepository;
        this.productoRepository = productoRepository;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /** Lista todas las solicitudes (para ADMINISTRADOR). */
    public List<SolicitudMaterial> listarTodas() {
        return solicitudRepository.findAllByOrderByCreatedAtDesc();
    }

    /** Lista todas las solicitudes de un veterinario (para vista del VET). */
    public List<SolicitudMaterial> listarPorSolicitante(Long personalId) {
        return solicitudRepository.findBySolicitanteIdOrderByCreatedAtDesc(personalId);
    }

    /** Lista solicitudes en estado PENDIENTE (para el panel del administrador). */
    public List<SolicitudMaterial> listarPendientes() {
        return solicitudRepository.findByEstadoOrderByCreatedAtDesc(EstadoSolicitud.PENDIENTE);
    }

    public SolicitudMaterial obtenerPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la solicitud con ID: " + id));
    }

    // ── Crear solicitud (VETERINARIO) ─────────────────────────────────────────

    /**
     * Registra una nueva solicitud de material por parte de un veterinario.
     *
     * @param solicitud Entidad con solicitante y producto ya asignados
     * @return La solicitud guardada
     */
    @Transactional
    public SolicitudMaterial guardar(SolicitudMaterial solicitud) {
        if (solicitud.getSolicitante() == null) {
            throw new IllegalArgumentException("El solicitante es obligatorio.");
        }
        if (solicitud.getProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }
        if (solicitud.getCantidadSolicitada() == null || solicitud.getCantidadSolicitada() < 1) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (solicitud.getMotivo() == null || solicitud.getMotivo().isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio.");
        }
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitudRepository.save(solicitud);
    }

    // ── Aprobar (ADMINISTRADOR) ───────────────────────────────────────────────

    /**
     * Aprueba una solicitud de material.
     * Cambia el estado a APROBADA pero NO descuenta stock todavía
     * (el stock se descuenta al marcar como ENTREGADA).
     *
     * @param solicitudId    ID de la solicitud
     * @param adminId        ID del administrador que aprueba
     * @param observacion    Comentario opcional
     */
    @Transactional
    public void aprobar(Long solicitudId, Long adminId, String observacion) {
        SolicitudMaterial solicitud = obtenerPorId(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden aprobar solicitudes en estado PENDIENTE. " +
                    "Estado actual: " + solicitud.getEstado());
        }

        Personal admin = personalRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado."));

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setAprobadoPor(admin);
        solicitud.setObservacionRespuesta(observacion);
        solicitud.setFechaRespuesta(OffsetDateTime.now());
        solicitudRepository.save(solicitud);
    }

    // ── Rechazar (ADMINISTRADOR) ──────────────────────────────────────────────

    /**
     * Rechaza una solicitud. El motivo de rechazo es obligatorio.
     *
     * @param solicitudId  ID de la solicitud
     * @param adminId      ID del administrador
     * @param motivoRechazo Explicación del rechazo (obligatorio)
     */
    @Transactional
    public void rechazar(Long solicitudId, Long adminId, String motivoRechazo) {
        if (motivoRechazo == null || motivoRechazo.isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio.");
        }

        SolicitudMaterial solicitud = obtenerPorId(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden rechazar solicitudes en estado PENDIENTE.");
        }

        Personal admin = personalRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado."));

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setAprobadoPor(admin);
        solicitud.setObservacionRespuesta(motivoRechazo);
        solicitud.setFechaRespuesta(OffsetDateTime.now());
        solicitudRepository.save(solicitud);
    }

    // ── Marcar como entregada (ADMINISTRADOR) ─────────────────────────────────

    /**
     * Marca la solicitud como entregada y descuenta el stock del producto.
     * La cantidad entregada puede ser menor a la solicitada (entrega parcial).
     *
     * @param solicitudId        ID de la solicitud
     * @param cantidadEntregada  Cantidad real entregada (puede ser < cantidadSolicitada)
     * @throws IllegalArgumentException si el stock es insuficiente
     */
    @Transactional
    public void marcarEntregada(Long solicitudId, Integer cantidadEntregada) {
        SolicitudMaterial solicitud = obtenerPorId(solicitudId);

        if (solicitud.getEstado() != EstadoSolicitud.APROBADA) {
            throw new IllegalStateException(
                    "Solo se pueden marcar como entregadas solicitudes en estado APROBADA.");
        }
        if (cantidadEntregada == null || cantidadEntregada < 1) {
            throw new IllegalArgumentException("La cantidad entregada debe ser mayor a cero.");
        }
        if (cantidadEntregada > solicitud.getCantidadSolicitada()) {
            throw new IllegalArgumentException(
                    "La cantidad entregada no puede superar la solicitada.");
        }

        // Descontar stock del inventario
        Producto producto = solicitud.getProducto();
        if (producto.getStockActual() < cantidadEntregada) {
            throw new IllegalArgumentException(
                    "Stock insuficiente en inventario. Disponible: " + producto.getStockActual());
        }
        producto.setStockActual(producto.getStockActual() - cantidadEntregada);
        productoRepository.save(producto);

        solicitud.setCantidadEntregada(cantidadEntregada);
        solicitud.setEstado(EstadoSolicitud.ENTREGADA);
        solicitud.setFechaRespuesta(OffsetDateTime.now());
        solicitudRepository.save(solicitud);
    }
}
