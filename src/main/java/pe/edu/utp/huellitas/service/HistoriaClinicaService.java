package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.HistoriaClinica;
import pe.edu.utp.huellitas.repository.HistoriaClinicaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Servicio de gestión de historia clínica.
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
        return historiaClinicaRepository.findByPacienteIdOrderByFechaDesc(pacienteId);
    }

    /**
     * Busca historias clínicas por nombre de paciente o propietario y/o por
     * rango de fecha de consulta (desde/hasta). Todos los parámetros son opcionales.
     *
     * @param buscar texto a buscar en el nombre del paciente o del propietario
     * @param desde  fecha inicial (inclusive), o null para no filtrar
     * @param hasta  fecha final (inclusive), o null para no filtrar
     */
    public List<HistoriaClinica> buscar(String buscar, LocalDate desde, LocalDate hasta) {
        String textoBusqueda = (buscar != null && !buscar.trim().isEmpty()) ? buscar.trim() : null;
        ZoneId zona = ZoneId.systemDefault();
        OffsetDateTime desdeDateTime = (desde != null)
                ? desde.atStartOfDay(zona).toOffsetDateTime()
                : null;
        OffsetDateTime hastaDateTime = (hasta != null)
                ? hasta.atTime(LocalTime.MAX).atZone(zona).toOffsetDateTime()
                : null;
        return historiaClinicaRepository.buscar(textoBusqueda, desdeDateTime, hastaDateTime);
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
     * @param historiaClinica Entidad con los datos de la consulta
     * @return La historia clínica guardada
     */
    @Transactional
    public HistoriaClinica guardar(HistoriaClinica historiaClinica, @org.springframework.context.annotation.Lazy PacienteService pacienteService) {
        if (historiaClinica.getPaciente() == null) {
            throw new IllegalArgumentException("El paciente es obligatorio.");
        }
        if (historiaClinica.getPersonal() == null) {
            throw new IllegalArgumentException("El veterinario tratante es obligatorio.");
        }
        
        HistoriaClinica saved = historiaClinicaRepository.save(historiaClinica);
        if (historiaClinica.getPesoKg() != null) {
            pacienteService.actualizarPesoReferencia(historiaClinica.getPaciente().getId(), historiaClinica.getPesoKg());
        }
        
        return saved;
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
    
    public boolean tieneHistoria(Long pacienteId) {
        return historiaClinicaRepository.existsByPacienteId(pacienteId);
    }
}
