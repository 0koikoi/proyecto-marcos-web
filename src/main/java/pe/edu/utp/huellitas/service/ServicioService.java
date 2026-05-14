package pe.edu.utp.huellitas.service;

import org.springframework.stereotype.Service;
import pe.edu.utp.huellitas.model.Servicio;
import pe.edu.utp.huellitas.repository.ServicioRepository;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicioService {

    private final ServicioRepository repo;

    public ServicioService(ServicioRepository repo) {
        this.repo = repo;
    }

    public List<Servicio> listarTodos() {
        return repo.findAll();
    }

    public void guardar(Servicio s) {
        // Validación obligatoria: El precio no puede ser negativo
        if (s.getPrecio() != null && s.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        repo.save(s);
    }

    public Servicio buscarPorId(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}