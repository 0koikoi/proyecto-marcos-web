package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.huellitas.model.Servicio;
import pe.edu.utp.huellitas.service.ServicioService;

@Controller
@RequestMapping("/servicios")
public class ServicioController {

    private final ServicioService service;

    public ServicioController(ServicioService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicios", service.listarTodos());
        model.addAttribute("activePage", "servicios");
        return "servicios";
    }

    @GetMapping("/nuevo")
    public String formulario(Model model) {
        model.addAttribute("servicio", new Servicio());
        return "formulario-servicio";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Servicio servicio) {
        service.guardar(servicio);
        return "redirect:/servicios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model) {
        Servicio servicio = service.buscarPorId(id);
        if (servicio != null) {
            model.addAttribute("servicio", servicio);
            // Reutilizamos el mismo formulario de 'nuevo' para editar
            return "formulario-servicio";
        }
        return "redirect:/servicios";
    }
}

