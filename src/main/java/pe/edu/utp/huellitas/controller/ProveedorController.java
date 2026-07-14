package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import jakarta.servlet.http.HttpSession;
import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.model.Proveedor;
import pe.edu.utp.huellitas.dto.ProveedorDTO;
import pe.edu.utp.huellitas.service.ProveedorService;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    // MOSTRAR VISTA + LISTAR
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("listaProveedores", proveedorService.listar());
        model.addAttribute("nuevoProveedor", new ProveedorDTO());
        model.addAttribute("activePage", "proveedores"); // Esto activa el color azul en el menú
        return "proveedores";
    }

    // GUARDAR
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("nuevoProveedor") Proveedor proveedor, BindingResult result, Model model, HttpSession session) {
        Personal usuario = (Personal) session.getAttribute("usuario");
        if (usuario == null || !usuario.getCargo().equals("ADMINISTRADOR")) {
            return "redirect:/dashboard?error=AccesoDenegado";
        }

        if (result.hasErrors()) {
            model.addAttribute("listaProveedores", proveedorService.listar());
            model.addAttribute("activePage", "proveedores");
            return "proveedores";
        }
        proveedorService.guardar(proveedor);

        return "redirect:/proveedores";
    }

    // ELIMINAR
    @GetMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id,
            HttpSession session) {

        Personal usuario = (Personal) session.getAttribute("usuario");

        if (usuario == null ||
                !usuario.getCargo().equals("ADMINISTRADOR")) {

            return "redirect:/dashboard?error=AccesoDenegado";
        }

        proveedorService.eliminar(id);

        return "redirect:/proveedores";
    }
    // editar

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.buscarPorId(id);
        model.addAttribute("nuevoProveedor", proveedor);
        model.addAttribute("listaProveedores", proveedorService.listar());
        model.addAttribute("activePage", "proveedores");
        return "proveedores";
    }
}
