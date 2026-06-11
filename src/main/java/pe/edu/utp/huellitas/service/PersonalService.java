package pe.edu.utp.huellitas.service;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.repository.PersonalRepository;

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

        repository.save(personal);
        return null; // Null significa que no hay errores
    }

}

