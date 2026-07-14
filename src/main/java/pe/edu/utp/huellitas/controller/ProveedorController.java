package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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
    public String guardar(@Valid @ModelAttribute("nuevoProveedor") ProveedorDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("listaProveedores", proveedorService.listar());
            model.addAttribute("activePage", "proveedores");
            return "proveedores";
        }
        
        Proveedor proveedor = null;
        if (dto.getId() != null) {
            proveedor = proveedorService.buscarPorId(dto.getId());
            if (proveedor == null) {
                proveedor = new Proveedor();
            }
        } else {
            proveedor = new Proveedor();
        }
        
        proveedor.setRuc(dto.getRuc());
        proveedor.setRazonSocial(dto.getRazonSocial());
        proveedor.setContacto(dto.getContacto());
        proveedor.setTelefono(dto.getTelefono());
        
        proveedorService.guardar(proveedor);

        return "redirect:/proveedores";
    }

    // ELIMINAR
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {

        proveedorService.eliminar(id);

        return "redirect:/proveedores";
    }
}
