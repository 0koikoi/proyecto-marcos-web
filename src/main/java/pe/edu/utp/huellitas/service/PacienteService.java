package pe.edu.utp.huellitas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.huellitas.model.Paciente;
import pe.edu.utp.huellitas.model.Propietario;
import pe.edu.utp.huellitas.repository.PacienteRepository;
import pe.edu.utp.huellitas.repository.PropietarioRepository;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PropietarioRepository propietarioRepository;

    public PacienteService(PacienteRepository pacienteRepository, PropietarioRepository propietarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.propietarioRepository = propietarioRepository;
    }

    public List<Paciente> listarTodos(String buscar) {
        if (buscar != null && !buscar.trim().isEmpty()) {
            String b = buscar.trim();
            return pacienteRepository.findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCaseOrPropietarioNombreCompletoContainingIgnoreCase(b, b, b);
        }
        return pacienteRepository.findAll();
    }

    public Paciente obtenerPorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el paciente con ID: " + id));
    }

    @Transactional
    public Paciente guardar(Paciente paciente) {
        validarPaciente(paciente);

        Long propietarioId = paciente.getPropietario().getId();

        Propietario propietario = propietarioRepository.findById(propietarioId)
                .orElseThrow(() -> new IllegalArgumentException("El propietario seleccionado no existe."));

        paciente.setPropietario(propietario);
        paciente.setNombre(paciente.getNombre().trim());
        paciente.setEspecie(paciente.getEspecie().trim());

        if (paciente.getRaza() != null) {
            paciente.setRaza(paciente.getRaza().trim());
        }

        if (paciente.getGenero() != null) {
            paciente.setGenero(paciente.getGenero().trim());
        }

        return pacienteRepository.save(paciente);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el paciente con ID: " + id);
        }

        pacienteRepository.deleteById(id);
    }

    private void validarPaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("El paciente no puede ser nulo.");
        }

        if (estaVacio(paciente.getNombre())) {
            throw new IllegalArgumentException("El nombre del paciente es obligatorio.");
        }

        if (estaVacio(paciente.getEspecie())) {
            throw new IllegalArgumentException("La especie es obligatoria.");
        }

        if (paciente.getPropietario() == null || paciente.getPropietario().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un propietario para el paciente.");
        }
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}

