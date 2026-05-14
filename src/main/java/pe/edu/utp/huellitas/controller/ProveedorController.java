package pe.edu.utp.huellitas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import pe.edu.utp.huellitas.model.Proveedor;
import pe.edu.utp.huellitas.service.ProveedorService;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    // MOSTRAR VISTA + LISTAR
  @GetMapping
public String listar(Model model) {
    model.addAttribute("listaProveedores", proveedorService.listar());
    model.addAttribute("nuevoProveedor", new Proveedor());
    model.addAttribute("activePage", "proveedores"); // Esto activa el color azul en el menú
    return "proveedores";
}
    

    // GUARDAR
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Proveedor proveedor) {

        proveedorService.guardar(proveedor);

        return "redirect:/proveedores";
    }

    // ELIMINAR
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {

        proveedorService.eliminar(id);

        return "redirect:/proveedores";
    }
}