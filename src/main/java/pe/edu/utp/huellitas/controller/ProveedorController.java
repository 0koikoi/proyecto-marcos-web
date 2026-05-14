package pe.edu.utp.huellitas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.huellitas.model.Proveedor;
import pe.edu.utp.huellitas.service.ProveedorService;

import java.util.List;

@RestController
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    // LISTAR
    @GetMapping
    public List<Proveedor> listar() {
        return proveedorService.listar();
    }

    // GUARDAR
    @PostMapping
    public Proveedor guardar(@RequestBody Proveedor proveedor) {
        return proveedorService.guardar(proveedor);
    }

    // BUSCAR POR ID
    @GetMapping("/{id}")
    public Proveedor buscar(@PathVariable Long id) {
        return proveedorService.buscarPorId(id);
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
    }
}
