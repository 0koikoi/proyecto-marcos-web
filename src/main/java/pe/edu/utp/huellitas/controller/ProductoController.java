package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.service.ProductoService;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // MOSTRAR VISTA + LISTAR
  @GetMapping
public String listar(Model model) {
    model.addAttribute("listaProductos", productoService.listar());
    model.addAttribute("nuevoProducto", new Producto());
    model.addAttribute("activePage", "productos"); // Esto activa el color azul en el menú
    return "productos";
}

    // GUARDAR
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto) {

        productoService.guardar(producto);

        return "redirect:/productos";
    }

    // ELIMINAR
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {

        productoService.eliminar(id);

        return "redirect:/productos";
    }
}

