package pe.edu.utp.huellitas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.huellitas.model.Paciente;
import pe.edu.utp.huellitas.model.Propietario;
import pe.edu.utp.huellitas.repository.PacienteRepository;


@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final pe.edu.utp.huellitas.service.PropietarioService propietarioService;
    private final org.springframework.context.ApplicationContext context;

    public PacienteService(PacienteRepository pacienteRepository, pe.edu.utp.huellitas.service.PropietarioService propietarioService, org.springframework.context.ApplicationContext context) {
        this.pacienteRepository = pacienteRepository;
        this.propietarioService = propietarioService;
        this.context = context;
    }

    public List<Paciente> listarTodos(String buscar) {
        if (buscar != null && !buscar.trim().isEmpty()) {
            String b = buscar.trim();
            return pacienteRepository.findByNombreContainingIgnoreCaseOrEspecieContainingIgnoreCaseOrPropietarioNombresContainingIgnoreCaseOrPropietarioApellidosContainingIgnoreCase(b, b, b, b);
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

        Propietario propietario = propietarioService.obtenerPorId(propietarioId);

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
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el paciente con ID: " + id));

        pe.edu.utp.huellitas.service.HistoriaClinicaService historiaService = context.getBean(pe.edu.utp.huellitas.service.HistoriaClinicaService.class);
        pe.edu.utp.huellitas.service.CitaService citaService = context.getBean(pe.edu.utp.huellitas.service.CitaService.class);

        if (historiaService.tieneHistoria(id) || citaService.tieneCitas(id)) {
            paciente.setEstado("INACTIVO");
            pacienteRepository.save(paciente);
        } else {
            pacienteRepository.deleteById(id);
        }
    }
    
    public boolean existeSimilar(Long propietarioId, String nombre, String especie) {
        List<Paciente> similares = pacienteRepository.buscarSimilares(propietarioId, nombre, especie);
        return !similares.isEmpty();
    }
    
    public boolean tienePacientes(Long propietarioId) {
        return !pacienteRepository.findByPropietarioId(propietarioId).isEmpty();
    }
    
    @Transactional
    public void actualizarPesoReferencia(Long id, java.math.BigDecimal peso) {
        pacienteRepository.findById(id).ifPresent(p -> {
            p.setPesoReferencia(peso);
            pacienteRepository.save(p);
        });
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

