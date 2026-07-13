package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.Receta;
import pe.edu.utp.huellitas.repository.RecetaRepository;

import java.util.List;

/**
 * Servicio de gestión de recetas médicas.
 *
 * ════════════════════════════════════════════════════════════
 * TODO — MÓDULO A IMPLEMENTAR POR EL EQUIPO
 * ════════════════════════════════════════════════════════════
 *
 * La estructura base está lista. El desarrollador asignado debe:
 *
 *   1. Crear RecetaController en package controller/
 *
 *   2. Crear las vistas Thymeleaf en templates/recetas/:
 *      - lista.html     → listado de recetas (filtrable por historia clínica)
 *      - formulario.html → crear/editar receta con sus líneas (DetalleReceta)
 *      - detalle.html   → vista imprimible de la receta con todos sus detalles
 *
 *   3. El formulario de receta debe manejar múltiples líneas (DetalleReceta)
 *      usando JavaScript para agregar/quitar medicamentos dinámicamente.
 *
 *   4. Agregar @PreAuthorize("hasAnyRole('ADMINISTRADOR','VETERINARIO')")
 *
 * Rutas esperadas del controller:
 *   GET  /recetas/historia/{id}      → recetas de una historia clínica
 *   GET  /recetas/nueva              → formulario nueva receta
 *   POST /recetas/guardar            → guardar con sus detalles
 *   GET  /recetas/{id}               → detalle / vista de impresión
 *   POST /recetas/eliminar/{id}      → eliminar (solo ADMIN)
 * ════════════════════════════════════════════════════════════
 */
@Service
public class RecetaService {

    private final RecetaRepository recetaRepository;

    public RecetaService(RecetaRepository recetaRepository) {
        this.recetaRepository = recetaRepository;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * Lista todas las recetas asociadas a una historia clínica.
     */
    public List<Receta> listarPorHistoria(Long historiaClinicaId) {
        return recetaRepository.findByHistoriaClinicaId(historiaClinicaId);
    }

    /**
     * Obtiene una receta por su ID, incluyendo sus líneas de detalle.
     *
     * @throws IllegalArgumentException si no existe
     */
    public Receta obtenerPorId(Long id) {
        return recetaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la receta con ID: " + id));
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    /**
     * Guarda una receta con todas sus líneas de medicamento.
     *
     * Las líneas (DetalleReceta) son manejadas en cascada gracias a
     * CascadeType.ALL + orphanRemoval en la entidad Receta.
     *
     * TODO: Validar que cada DetalleReceta tenga medicamento, dosis y frecuencia.
     *
     * @param receta Entidad con la lista de detalles ya asignada
     * @return La receta guardada con su ID asignado
     */
    @Transactional
    public Receta guardar(Receta receta) {
        if (receta.getHistoriaClinica() == null) {
            throw new IllegalArgumentException("La historia clínica es obligatoria.");
        }
        if (receta.getPersonal() == null) {
            throw new IllegalArgumentException("El veterinario firmante es obligatorio.");
        }
        if (receta.getDetalles() == null || receta.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La receta debe tener al menos un medicamento.");
        }
        // Asegurar que cada detalle apunte a esta receta
        receta.getDetalles().forEach(detalle -> detalle.setReceta(receta));
        return recetaRepository.save(receta);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    /** Los detalles se eliminan en cascada (CascadeType.ALL + orphanRemoval). */
    @Transactional
    public void eliminar(Long id) {
        if (!recetaRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró la receta con ID: " + id);
        }
        recetaRepository.deleteById(id);
    }
}
