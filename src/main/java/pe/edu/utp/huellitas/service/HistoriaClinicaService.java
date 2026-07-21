package pe.edu.utp.huellitas.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.exception.NegocioException;
import pe.edu.utp.huellitas.model.HistoriaClinica;
import pe.edu.utp.huellitas.repository.HistoriaClinicaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Servicio de gestión de historia clínica.
 *
 * Nota: HistoriaClinicaService y PacienteService se necesitan mutuamente
 * (peso automático hacia Paciente, chequeo de historial hacia HistoriaClinica
 * antes de eliminar un paciente), lo que genera una dependencia circular real.
 * Siguiendo el mismo patrón ya usado en main, PacienteService se recibe como
 * parámetro de método con @Lazy en vez de inyectarse por constructor.
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
     * @throws NegocioException si no existe
     */
    public HistoriaClinica obtenerPorId(Long id) {
        return historiaClinicaRepository.findById(id)
                .orElseThrow(() -> new NegocioException(
                        "No se encontró la historia clínica con ID: " + id));
    }

    // ── Guardar ───────────────────────────────────────────────────────────────

    /**
     * Guarda una nueva entrada de historia clínica.
     *
     * Regla de negocio: si la consulta trae un peso registrado, el peso de
     * referencia del paciente se actualiza automáticamente — nunca se edita
     * a mano en el formulario de paciente.
     *
     * @param historiaClinica Entidad con los datos de la consulta
     * @param pacienteService inyectado @Lazy para evitar dependencia circular
     *                        (PacienteService también depende de este servicio
     *                        para saber si un paciente tiene historial antes de eliminarlo)
     * @return La historia clínica guardada
     */
    @Transactional
    public HistoriaClinica guardar(HistoriaClinica historiaClinica, @Lazy PacienteService pacienteService) {
        if (historiaClinica.getPaciente() == null) {
            throw new NegocioException("El paciente es obligatorio.");
        }
        if (historiaClinica.getPersonal() == null) {
            throw new NegocioException("El veterinario tratante es obligatorio.");
        }
        HistoriaClinica guardada = historiaClinicaRepository.save(historiaClinica);
        if (historiaClinica.getPesoKg() != null) {
            pacienteService.actualizarPesoReferencia(historiaClinica.getPaciente().getId(), historiaClinica.getPesoKg());
        }
        return guardada;
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    /**
     * Elimina una historia clínica. Solo ADMINISTRADOR.
     * ADVERTENCIA: Verificar que no tenga recetas ni vacunas asociadas antes de eliminar.
     */
    @Transactional
    public void eliminar(Long id) {
        if (!historiaClinicaRepository.existsById(id)) {
            throw new NegocioException("No se encontró la historia clínica con ID: " + id);
        }
        historiaClinicaRepository.deleteById(id);
    }

    /**
     * Usado por PacienteService.eliminar(): si el paciente tiene historial clínico,
     * se desactiva en vez de borrarse físicamente (evita perder historial vinculado).
     */
    public boolean tieneHistoria(Long pacienteId) {
        return historiaClinicaRepository.existsByPacienteId(pacienteId);
    }
}
