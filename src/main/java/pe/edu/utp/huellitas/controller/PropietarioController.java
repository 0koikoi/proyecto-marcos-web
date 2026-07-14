package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import pe.edu.utp.huellitas.model.Propietario;
import pe.edu.utp.huellitas.dto.PropietarioDTO;
import pe.edu.utp.huellitas.service.PropietarioService;

@Controller
@RequestMapping("/propietarios")
public class PropietarioController {

    private final PropietarioService propietarioService;

    public PropietarioController(PropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    @GetMapping
    public String listar(@org.springframework.web.bind.annotation.RequestParam(required = false) String buscar,
            Model model) {
        model.addAttribute("propietarios", propietarioService.buscarPropietarios(buscar));
        model.addAttribute("buscar", buscar);
        return "propietarios/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        PropietarioDTO propietario = new PropietarioDTO();
        propietario.setCorreo("@gmail.com");

        model.addAttribute("propietario", propietario);
        model.addAttribute("titulo", "Registrar propietario");
        return "propietarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("propietario") PropietarioDTO dto,
                          BindingResult result,
                          Model model) {

        if (result.hasErrors()) {
            model.addAttribute("propietario", dto);
            model.addAttribute("titulo", dto.getId() == null ? "Registrar propietario" : "Editar propietario");
            return "propietarios/form";
        }

        try {
            Propietario propietario = null;
            if (dto.getId() != null) {
                propietario = propietarioService.obtenerPorId(dto.getId());
            } else {
                propietario = new Propietario();
            }
            
            propietario.setDni(dto.getDni());
            propietario.setNombreCompleto(dto.getNombreCompleto());
            propietario.setTelefono(dto.getTelefono());
            propietario.setCorreo(dto.getCorreo());
            propietario.setDireccion(dto.getDireccion());
            
            propietarioService.guardar(propietario);
            return "redirect:/propietarios";
        } catch (IllegalArgumentException e) {
            model.addAttribute("propietario", dto);
            model.addAttribute("titulo", dto.getId() == null ? "Registrar propietario" : "Editar propietario");
            model.addAttribute("error", e.getMessage());
            return "propietarios/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Propietario prop = propietarioService.obtenerPorId(id);
        PropietarioDTO dto = new PropietarioDTO();
        dto.setId(prop.getId());
        dto.setDni(prop.getDni());
        dto.setNombreCompleto(prop.getNombreCompleto());
        dto.setTelefono(prop.getTelefono());
        dto.setCorreo(prop.getCorreo());
        dto.setDireccion(prop.getDireccion());

        model.addAttribute("propietario", dto);
        model.addAttribute("titulo", "Editar propietario");
        return "propietarios/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        propietarioService.eliminar(id);
        return "redirect:/propietarios";
    }
}
