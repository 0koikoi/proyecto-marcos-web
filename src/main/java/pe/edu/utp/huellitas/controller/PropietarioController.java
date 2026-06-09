package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import pe.edu.utp.huellitas.model.Propietario;
import pe.edu.utp.huellitas.service.PropietarioService;

@Controller
@RequestMapping("/propietarios")
public class PropietarioController {

    private final PropietarioService propietarioService;

    public PropietarioController(PropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("propietarios", propietarioService.listarTodos());
        return "propietarios/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Propietario propietario = new Propietario();
        propietario.setTelefono("+51 9");
        propietario.setCorreo("@gmail.com");
        
        model.addAttribute("propietario", propietario);
        model.addAttribute("titulo", "Registrar propietario");
        return "propietarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Propietario propietario, Model model) {
        try {
            propietarioService.guardar(propietario);
            return "redirect:/propietarios";
        } catch (IllegalArgumentException e) {
            model.addAttribute("propietario", propietario);
            model.addAttribute("titulo", propietario.getId() == null ? "Registrar propietario" : "Editar propietario");
            model.addAttribute("error", e.getMessage());
            return "propietarios/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("propietario", propietarioService.obtenerPorId(id));
        model.addAttribute("titulo", "Editar propietario");
        return "propietarios/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        propietarioService.eliminar(id);
        return "redirect:/propietarios";
    }
}

