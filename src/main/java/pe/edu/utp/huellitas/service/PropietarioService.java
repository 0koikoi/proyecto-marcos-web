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

    public PropietarioService(PropietarioRepository propietarioRepository) {
        this.propietarioRepository = propietarioRepository;
    }

    public List<Propietario> listarTodos() {
        return propietarioRepository.findAll();
    }

    public Propietario obtenerPorId(Long id) {
        return propietarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el propietario con ID: " + id));
    }

    @Transactional
    public Propietario guardar(Propietario propietario) {
        validarPropietario(propietario);

        propietario.setDni(propietario.getDni().trim());
        propietario.setNombreCompleto(propietario.getNombreCompleto().trim());
        propietario.setTelefono(propietario.getTelefono().trim());

        if (propietario.getCorreo() != null) {
            propietario.setCorreo(propietario.getCorreo().trim());
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

        if (estaVacio(propietario.getNombreCompleto())) {
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        }

        if (estaVacio(propietario.getTelefono())) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }

        if (!propietario.getTelefono().matches("^(\\+51\\s?)?9\\d{8}$")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 9 dígitos.");
        }
        if (estaVacio(propietario.getDireccion())) {
            throw new IllegalArgumentException("La dirección es obligatoria.");
        }
    }
    

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}

