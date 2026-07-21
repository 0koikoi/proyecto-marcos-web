package pe.edu.utp.huellitas.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.huellitas.model.Propietario;
import pe.edu.utp.huellitas.repository.PropietarioRepository;

@Service
public class PropietarioService {

    private final PropietarioRepository propietarioRepository;
    private final pe.edu.utp.huellitas.repository.PacienteRepository pacienteRepository;

    public PropietarioService(PropietarioRepository propietarioRepository, pe.edu.utp.huellitas.repository.PacienteRepository pacienteRepository) {
        this.propietarioRepository = propietarioRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public List<Propietario> listarTodos() {
        return propietarioRepository.findAll();
    }

    public List<Propietario> buscarPropietarios(String buscar) {
        if (estaVacio(buscar)) {
            return listarTodos();
        }
        return propietarioRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDniContaining(buscar, buscar, buscar);
    }

    public Propietario obtenerPorId(Long id) {
        return propietarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el propietario con ID: " + id));
    }

    @Transactional
    public Propietario guardar(Propietario propietario) {
        validarPropietario(propietario);

        propietario.setDni(propietario.getDni().trim());
        if (propietario.getNombres() != null) propietario.setNombres(propietario.getNombres().trim());
        if (propietario.getApellidos() != null) propietario.setApellidos(propietario.getApellidos().trim());
        propietario.setTelefono(propietario.getTelefono().trim());

        if (propietario.getEmail() != null) {
            propietario.setEmail(propietario.getEmail().trim());
        }

        if (propietario.getDireccion() != null) {
            propietario.setDireccion(propietario.getDireccion().trim());
        }

        return propietarioRepository.save(propietario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!propietarioRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el propietario con ID: " + id);
        }

        // REGLA DE NEGOCIO: Un propietario con pacientes, historia clínica, etc. no se puede eliminar físicamente.
        if (!pacienteRepository.findByPropietarioId(id).isEmpty()) {
            throw new pe.edu.utp.huellitas.exception.NegocioException("No se puede eliminar al propietario porque tiene pacientes asociados (historial clínico).");
        }

        propietarioRepository.deleteById(id);
    }

    private void validarPropietario(Propietario propietario) {
        if (propietario == null) {
            throw new IllegalArgumentException("El propietario no puede ser nulo.");
        }

        if (estaVacio(propietario.getDni())) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }

        if (!propietario.getDni().matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos.");
        }

        Optional<Propietario> propietarioExistente = propietarioRepository.findByDni(propietario.getDni());

        if (propietarioExistente.isPresent()
                && !propietarioExistente.get().getId().equals(propietario.getId())) {
            throw new IllegalArgumentException("Ya existe un propietario registrado con ese DNI.");
        }

        if (estaVacio(propietario.getNombres())) {
            throw new IllegalArgumentException("Los nombres son obligatorios.");
        }

        if (estaVacio(propietario.getTelefono())) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }

        if (!propietario.getTelefono().matches("^(\\+51\\s?)?9\\d{8}$")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 9 dígitos.");
        }
        // La dirección es opcional en la nueva versión
    }
    

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}

