package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import pe.edu.utp.huellitas.model.Producto;
import pe.edu.utp.huellitas.service.ProductoService;


import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/productos")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCION', 'VETERINARIO')")
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
        return "productos/lista";
    }

    // GUARDAR
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("nuevoProducto") Producto producto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("listaProductos", productoService.listar());
            model.addAttribute("activePage", "productos");
            return "productos/lista";
        }
        productoService.guardar(producto);
        return "redirect:/productos";
    }

    // ELIMINAR
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return "redirect:/productos";
    }
}

