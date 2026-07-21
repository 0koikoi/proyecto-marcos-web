package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.exception.NegocioException;
import pe.edu.utp.huellitas.model.DetalleReceta;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.model.Receta;
import pe.edu.utp.huellitas.repository.RecetaRepository;

import java.util.List;
import java.util.stream.Collectors;

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
     * @throws NegocioException si no existe
     */
    public Receta obtenerPorId(Long id) {
        return recetaRepository.findById(id)
                .orElseThrow(() -> new NegocioException(
                        "No se encontró la receta con ID: " + id));
    }

    /**
     * Devuelve los productos del inventario enlazados a los medicamentos de una receta.
     * Solo incluye las líneas de {@link DetalleReceta} que tienen un {@code producto} asociado
     * (el enlace es opcional en el formulario) — son los únicos que Ventas puede precargar
     * automáticamente al momento de facturar.
     *
     * @param recetaId ID de la receta
     * @return Lista de productos vendibles (puede estar vacía si ningún medicamento se enlazó)
     */
    public List<Producto> obtenerMedicamentosVendibles(Long recetaId) {
        Receta receta = obtenerPorId(recetaId);
        return receta.getDetalles().stream()
                .map(DetalleReceta::getProducto)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
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
            throw new NegocioException("La historia clínica es obligatoria.");
        }
        if (receta.getPersonal() == null) {
            throw new NegocioException("El veterinario firmante es obligatorio.");
        }
        if (receta.getDetalles() == null || receta.getDetalles().isEmpty()) {
            throw new NegocioException("La receta debe tener al menos un medicamento.");
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
            throw new NegocioException("No se encontró la receta con ID: " + id);
        }
        recetaRepository.deleteById(id);
    }
}
