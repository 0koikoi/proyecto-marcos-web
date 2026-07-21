package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.exception.NegocioException;
import pe.edu.utp.huellitas.model.Cita;
import pe.edu.utp.huellitas.model.EstadoCita;
import pe.edu.utp.huellitas.repository.CitaRepository;

import pe.edu.utp.huellitas.repository.PersonalRepository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Servicio de negocio para la gestión de citas veterinarias.
 *
 * Reglas de negocio aplicadas:
 *   - Un veterinario no puede tener dos citas activas solapadas en el tiempo.
 *   - Las transiciones de estado siguen el flujo:
 *       PENDIENTE → EN_PROCESO → COMPLETADA
 *       PENDIENTE → CANCELADA
 *   - No se puede cancelar ni completar una cita ya finalizada.
 *   - Solo se lanza NegocioException; nunca try/catch locales en el controller.
 */
@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final PersonalRepository personalRepository;

    public CitaService(CitaRepository citaRepository, PersonalRepository personalRepository) {
        this.citaRepository = citaRepository;
        this.personalRepository = personalRepository;
    }

    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    public List<Cita> buscarPorFiltros(String dni, OffsetDateTime start, OffsetDateTime end) {
        if (dni != null && dni.trim().isEmpty()) {
            dni = null;
        }
        return citaRepository.buscarPorFiltros(dni, start, end);
    }

    @Transactional
    public Cita guardar(Cita cita) {
        if (cita.getPaciente() == null) {
            throw new NegocioException("Debe seleccionar un paciente para la cita.");
        }
        if (cita.getPersonal() == null) {
            throw new NegocioException("Debe asignar un veterinario a la cita.");
        }
        if (cita.getFechaHora() == null) {
            throw new NegocioException("La fecha y hora de la cita son obligatorias.");
        }
        if (cita.getFechaHora().isBefore(OffsetDateTime.now()) && cita.getId() == null) {
            throw new NegocioException("No se puede registrar una cita en una fecha y hora pasada.");
        }

        pe.edu.utp.huellitas.model.Personal personalCompleto = personalRepository.findById(cita.getPersonal().getId())
                .orElseThrow(() -> new NegocioException("El personal seleccionado no existe."));

        if (!"VETERINARIO".equalsIgnoreCase(personalCompleto.getRol().getNombre())) {
            throw new NegocioException("El personal asignado a la cita debe ser un VETERINARIO.");
        }
        
        if (!Boolean.TRUE.equals(personalCompleto.getActivo())) {
            throw new NegocioException("El veterinario asignado se encuentra inactivo.");
        }

        // Duración por defecto si no se especifica
        int duracion = (cita.getDuracionMinutos() != null && cita.getDuracionMinutos() > 0)
                ? cita.getDuracionMinutos() : 30;
        cita.setDuracionMinutos(duracion);

        // Validación de solapamiento: ventana de tiempo de la cita
        OffsetDateTime inicio = cita.getFechaHora().minusMinutes(duracion - 1);
        OffsetDateTime fin = cita.getFechaHora().plusMinutes(duracion - 1);
        boolean solapamiento = citaRepository.existeSolapamiento(
                cita.getPersonal().getId(),
                inicio,
                fin,
                cita.getId() // null para nuevas, id para edición
        );
        if (solapamiento) {
            throw new NegocioException(
                "El veterinario ya tiene una cita activa en ese horario. " +
                "Por favor elige otra fecha u hora."
            );
        }

        // Validación de solapamiento para el paciente
        boolean solapamientoPaciente = citaRepository.existeSolapamientoPaciente(
                cita.getPaciente().getId(),
                inicio,
                fin,
                cita.getId()
        );
        if (solapamientoPaciente) {
            throw new NegocioException(
                "El paciente ya tiene una cita activa en ese horario."
            );
        }

        if (cita.getEstado() == null) {
            cita.setEstado(EstadoCita.PENDIENTE);
        }
        return citaRepository.save(cita);
    }

    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Cita no encontrada con id: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        citaRepository.deleteById(id);
    }

    @Transactional
    public void cancelar(Long id) {
        Cita cita = obtenerPorId(id);
        if (cita.getEstado() == EstadoCita.COMPLETADA || cita.getEstado() == EstadoCita.CANCELADA) {
            throw new NegocioException(
                "No se puede cancelar una cita que ya está en estado: " + cita.getEstado()
            );
        }
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Transactional
    public void iniciar(Long id) {
        Cita cita = obtenerPorId(id);
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new NegocioException(
                "Solo se pueden iniciar citas en estado PENDIENTE. Estado actual: " + cita.getEstado()
            );
        }
        cita.setEstado(EstadoCita.EN_PROCESO);
        citaRepository.save(cita);
    }

    @Transactional
    public void completar(Long id) {
        Cita cita = obtenerPorId(id);
        if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new NegocioException(
                "No se puede completar una cita en estado: " + cita.getEstado()
            );
        }
        cita.setEstado(EstadoCita.COMPLETADA);
        citaRepository.save(cita);
    }

    public boolean tieneCitas(Long pacienteId) {
        return citaRepository.existsByPacienteId(pacienteId);
    }
}