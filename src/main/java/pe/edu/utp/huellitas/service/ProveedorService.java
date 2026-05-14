package pe.edu.utp.huellitas.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pe.edu.utp.huellitas.model.Proveedor;
import pe.edu.utp.huellitas.repository.ProveedorRepository;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    // LISTAR
    public List<Proveedor> listar() {
        return proveedorRepository.findAll();
    }

    // GUARDAR
    public Proveedor guardar(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    // BUSCAR POR ID
    public Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id).orElse(null);
    }

    // ELIMINAR
    public void eliminar(Long id) {
        proveedorRepository.deleteById(id);
    }
}
