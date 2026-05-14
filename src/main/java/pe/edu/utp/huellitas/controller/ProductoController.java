package pe.edu.utp.huellitas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.service.ProductoService;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // LISTAR
    @GetMapping
    public List<Producto> listar() {
        return productoService.listar();
    }

    // GUARDAR
    @PostMapping
    public Producto guardar(@RequestBody Producto producto) {
        return productoService.guardar(producto);
    }

    // BUSCAR
    @GetMapping("/{id}")
    public Producto buscar(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}