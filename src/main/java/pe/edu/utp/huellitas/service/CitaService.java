package pe.edu.utp.huellitas.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.huellitas.model.Cita;
import pe.edu.utp.huellitas.model.EstadoCita;
import pe.edu.utp.huellitas.repository.CitaRepository;
import pe.edu.utp.huellitas.repository.HistoriaClinicaRepository;




@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    
    public CitaService(
        CitaRepository citaRepository,
        HistoriaClinicaRepository historiaClinicaRepository) {

    this.citaRepository = citaRepository;
    this.historiaClinicaRepository = historiaClinicaRepository;
}
    public List<Cita> listarTodas() {
    return citaRepository.findAll();
}
     

    private void validarSolapamiento(Cita cita) {

    OffsetDateTime inicioNueva = cita.getFechaHora();
    OffsetDateTime finNueva = inicioNueva.plusMinutes(cita.getDuracionMinutos());

    // ==========================
    // VALIDAR VETERINARIO
    // ==========================
    List<Cita> citasVeterinario =
            citaRepository.buscarCitasDelVeterinario(cita.getPersonal().getId());

    for (Cita existente : citasVeterinario) {

        if (cita.getId() != null &&
                cita.getId().equals(existente.getId())) {
            continue;
        }

        OffsetDateTime inicioExistente = existente.getFechaHora();
        OffsetDateTime finExistente =
                inicioExistente.plusMinutes(existente.getDuracionMinutos());

        boolean hayCruce =
                inicioNueva.isBefore(finExistente)
                        && finNueva.isAfter(inicioExistente);

        if (hayCruce) {
            throw new IllegalArgumentException(
                    "El veterinario ya tiene una cita en ese horario."
            );
        }
    }

    // ==========================
    // VALIDAR PACIENTE
    // ==========================
    List<Cita> citasPaciente =
            citaRepository.buscarCitasDelPaciente(cita.getPaciente().getId());

    for (Cita existente : citasPaciente) {

        if (cita.getId() != null &&
                cita.getId().equals(existente.getId())) {
            continue;
        }

        OffsetDateTime inicioExistente = existente.getFechaHora();
        OffsetDateTime finExistente =
                inicioExistente.plusMinutes(existente.getDuracionMinutos());

        boolean hayCruce =
                inicioNueva.isBefore(finExistente)
                        && finNueva.isAfter(inicioExistente);

        if (hayCruce) {
            throw new IllegalArgumentException(
                    "El paciente ya tiene una cita programada en ese horario."
            );
        }
    }
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

    validarSolapamiento(cita);

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

    if (cita.getEstado() == EstadoCita.CANCELADA ||
        cita.getEstado() == EstadoCita.COMPLETADA) {

        throw new IllegalStateException(
                "No se puede completar una cita " + cita.getEstado());
    }

    // Verificar que exista una Historia Clínica asociada
    if (!historiaClinicaRepository.existsByCitaId(id)) {
        throw new IllegalStateException(
                "No se puede completar la cita porque aún no tiene una Historia Clínica asociada.");
    }

    cita.setEstado(EstadoCita.COMPLETADA);

    citaRepository.save(cita);
}
    
    public boolean tieneCitas(Long pacienteId) {
        return citaRepository.existsByPacienteId(pacienteId);
    }

    
}