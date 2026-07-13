package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.HistoriaClinica;
import pe.edu.utp.huellitas.repository.HistoriaClinicaRepository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Servicio de gestión de historia clínica.
 *
 * ════════════════════════════════════════════════════════════
 * TODO — MÓDULO A IMPLEMENTAR POR EL EQUIPO
 * ════════════════════════════════════════════════════════════
 *
 * La estructura base está lista. El desarrollador asignado debe:
 *
 *   1. Crear HistoriaClinicaController en package controller/
 *      (ver comentario de rutas al final de esta clase).
 *
 *   2. Crear las vistas Thymeleaf en templates/historia/:
 *      - lista.html   → tabla de consultas del paciente
 *      - formulario.html → crear nueva entrada
 *      - detalle.html → ver diagnóstico, tratamiento, recetas y vacunas
 *
 *   3. Implementar el método completarCita(Long citaId) que:
 *      - Cambia el estado de la cita a COMPLETADA
 *      - Crea automáticamente una HistoriaClinica vinculada
 *
 *   4. Agregar @PreAuthorize("hasAnyRole('ADMINISTRADOR','VETERINARIO')")
 *      en cada endpoint del controller.
 *
 * Rutas esperadas del controller:
 *   GET  /historia                   → lista todas (paginado)
 *   GET  /historia/paciente/{id}     → historial de un paciente
 *   GET  /historia/nueva             → formulario nueva consulta
 *   POST /historia/guardar           → guardar consulta
 *   GET  /historia/{id}              → detalle de una consulta
 *   POST /historia/eliminar/{id}     → eliminar (solo ADMIN)
 * ════════════════════════════════════════════════════════════
 */
@Service
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaClinicaRepository;

    public HistoriaClinicaService(HistoriaClinicaRepository historiaClinicaRepository) {
        this.historiaClinicaRepository = historiaClinicaRepository;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /** Lista todas las historias clínicas (para vista de administrador). */
    public List<HistoriaClinica> listarTodas() {
        return historiaClinicaRepository.findAll();
    }

    /**
     * Lista el historial clínico completo de un paciente, ordenado por fecha descendente.
     *
     * @param pacienteId ID del paciente
     * @return Lista de entradas de historia clínica, más reciente primero
     */
    public List<HistoriaClinica> listarPorPaciente(Long pacienteId) {
        return historiaClinicaRepository.findByPacienteIdOrderByFechaConsultaDesc(pacienteId);
    }

    /**
     * Obtiene una entrada de historia clínica por ID.
     *
     * @throws IllegalArgumentException si no existe
     */
    public HistoriaClinica obtenerPorId(Long id) {
        return historiaClinicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la historia clínica con ID: " + id));
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    /**
     * Guarda una nueva entrada de historia clínica.
     *
     * TODO: Implementar validaciones adicionales:
     *   - El veterinario asignado debe tener rol VETERINARIO.
     *   - Si viene de una cita (cita_id != null), cambiar estado de la cita a COMPLETADA.
     *
     * @param historiaClinica Entidad con los datos de la consulta
     * @return La historia clínica guardada
     */
    @Transactional
    public HistoriaClinica guardar(HistoriaClinica historiaClinica) {
        if (historiaClinica.getPaciente() == null) {
            throw new IllegalArgumentException("El paciente es obligatorio.");
        }
        if (historiaClinica.getPersonal() == null) {
            throw new IllegalArgumentException("El veterinario tratante es obligatorio.");
        }
        if (historiaClinica.getMotivoConsulta() == null || historiaClinica.getMotivoConsulta().isBlank()) {
            throw new IllegalArgumentException("El motivo de consulta es obligatorio.");
        }
        historiaClinica.setUpdatedAt(OffsetDateTime.now());
        return historiaClinicaRepository.save(historiaClinica);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    /**
     * Elimina una historia clínica. Solo ADMINISTRADOR.
     * ADVERTENCIA: Verificar que no tenga recetas ni vacunas asociadas antes de eliminar.
     */
    @Transactional
    public void eliminar(Long id) {
        if (!historiaClinicaRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró la historia clínica con ID: " + id);
        }
        historiaClinicaRepository.deleteById(id);
    }
}
