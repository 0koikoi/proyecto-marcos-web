package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.Receta;
import pe.edu.utp.huellitas.repository.RecetaRepository;

import java.util.List;

/**
 * Servicio de gestión de recetas médicas.
 */
@Service
public class RecetaService {

    private final RecetaRepository recetaRepository;

    public RecetaService(RecetaRepository recetaRepository) {
        this.recetaRepository = recetaRepository;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /** Lista todas las recetas registradas. */
    public List<Receta> listarTodas() {
        return recetaRepository.findAll();
    }

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
