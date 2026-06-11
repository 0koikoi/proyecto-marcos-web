package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.model.Cita;
import pe.edu.utp.huellitas.repository.CitaRepository;
import java.util.List;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    @Transactional
    public Cita guardar(Cita cita) {
        if (cita.getPaciente() == null || cita.getPersonal() == null) {
            throw new IllegalArgumentException("La cita debe tener un paciente y un personal asignado.");
        }
        return citaRepository.save(cita);
    }

    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id).orElse(null);
    }

    @Transactional
    public void eliminar(Long id) {
        citaRepository.deleteById(id);
    }
}