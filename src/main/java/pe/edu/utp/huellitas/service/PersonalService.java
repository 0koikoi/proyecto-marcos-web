package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.repository.PersonalRepository;
import java.util.List;

@Service
public class PersonalService {

    private final PersonalRepository repository;

    public PersonalService(PersonalRepository repository) {
        this.repository = repository;
    }

    public List<Personal> listarTodos() {
        return repository.findAll();
    }

    public String guardar(Personal personal) {
        // Validación de negocio
        if (!personal.getCodigoInstitucional().matches("^C\\d{6}$")) {
            return "Error: El código debe empezar con 'C' seguido de 6 dígitos.";
        }

        // Simulación de encriptación de contraseña (para el avance)
        if(personal.getId() == null) {
            personal.setPasswordHash("{noop}" + personal.getPasswordHash());
        }

        repository.save(personal);
        return null; // Null significa que no hay errores
    }
}

