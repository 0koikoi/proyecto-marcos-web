package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.Vacuna;
import pe.edu.utp.huellitas.repository.VacunaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de gestión de vacunación.
 */
@Service
public class VacunaService {

    private final VacunaRepository vacunaRepository;

    public VacunaService(VacunaRepository vacunaRepository) {
        this.vacunaRepository = vacunaRepository;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<Vacuna> listarTodas() {
        return vacunaRepository.findAll();
    }

    /**
     * Lista el historial de vacunación de un paciente específico,
     * ordenado por fecha de aplicación descendente.
     */
    public List<Vacuna> listarPorPaciente(Long pacienteId) {
        return vacunaRepository.findByPacienteIdOrderByFechaAplicacionDesc(pacienteId);
    }

    /**
     * Retorna las vacunas cuya próxima dosis está programada dentro
     * de los próximos {@code diasAnticipacion} días.
     * Usado para alertas en el dashboard.
     *
     * @param diasAnticipacion Número de días de anticipación (ej: 7)
     */
    public List<Vacuna> listarProximasDosis(int diasAnticipacion) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(diasAnticipacion);
        return vacunaRepository.findByFechaProximaDosisBetween(hoy, limite);
    }

    public Vacuna obtenerPorId(Long id) {
        return vacunaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la vacuna con ID: " + id));
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    /** Registra una nueva vacuna aplicada al paciente. */
    @Transactional
    public Vacuna guardar(Vacuna vacuna) {
        if (vacuna.getPaciente() == null) {
            throw new IllegalArgumentException("El paciente es obligatorio.");
        }
        if (vacuna.getPersonal() == null) {
            throw new IllegalArgumentException("El veterinario es obligatorio.");
        }
        if (vacuna.getNombre() == null || vacuna.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la vacuna es obligatorio.");
        }
        if (vacuna.getFechaAplicacion() == null) {
            throw new IllegalArgumentException("La fecha de aplicación es obligatoria.");
        }
        if (vacuna.getFechaProximaDosis() != null &&
                vacuna.getFechaProximaDosis().isBefore(vacuna.getFechaAplicacion())) {
            throw new IllegalArgumentException(
                    "La fecha de próxima dosis debe ser posterior a la fecha de aplicación.");
        }
        return vacunaRepository.save(vacuna);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @Transactional
    public void eliminar(Long id) {
        if (!vacunaRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró la vacuna con ID: " + id);
        }
        vacunaRepository.deleteById(id);
    }
}
