package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.Cita;
import pe.edu.utp.huellitas.model.EstadoCita;
import pe.edu.utp.huellitas.repository.CitaRepository;
import java.util.List;
import java.time.OffsetDateTime;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
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
        if (cita.getPaciente() == null || cita.getPersonal() == null) {
            throw new IllegalArgumentException("La cita debe tener un paciente y un veterinario asignado.");
        }
        if (cita.getFechaHora() == null) {
            throw new IllegalArgumentException("La fecha y hora de la cita son obligatorias.");
        }
        if (cita.getEstado() == null) {
            cita.setEstado(EstadoCita.PENDIENTE);
        }
        return citaRepository.save(cita);
    }

    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con id: " + id));
    }

    @Transactional
    public void eliminar(Long id) {
        citaRepository.deleteById(id);
    }

    @Transactional
    public void cancelar(Long id) {
        Cita cita = obtenerPorId(id);
        if (cita.getEstado() == EstadoCita.COMPLETADA || cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException("No se puede cancelar una cita que ya está " + cita.getEstado());
        }
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Transactional
    public void iniciar(Long id) {
        Cita cita = obtenerPorId(id);
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden iniciar citas en estado PENDIENTE.");
        }
        cita.setEstado(EstadoCita.EN_PROCESO);
        citaRepository.save(cita);
    }

    @Transactional
    public void completar(Long id) {
        Cita cita = obtenerPorId(id);
        if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new IllegalStateException("No se puede completar una cita " + cita.getEstado());
        }
        cita.setEstado(EstadoCita.COMPLETADA);
        citaRepository.save(cita);
    }
}