package pe.edu.utp.huellitas.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.huellitas.model.Cita;
import pe.edu.utp.huellitas.service.CitaService;
import pe.edu.utp.huellitas.service.PacienteService;
import pe.edu.utp.huellitas.service.PersonalService;

@Controller
@RequestMapping("/citas")
public class CitaController {

    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final PersonalService personalService;

    public CitaController(CitaService citaService, PacienteService pacienteService, PersonalService personalService) {
        this.citaService = citaService;
        this.pacienteService = pacienteService;
        this.personalService = personalService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("citas", citaService.listarTodas());
        model.addAttribute("activePage", "citas");
        return "citas";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("personal", personalService.listarTodos());
        model.addAttribute("activePage", "citas");
        return "formulario-cita";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cita") Cita cita, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pacientes", pacienteService.listarTodos(null));
            model.addAttribute("personal", personalService.listarTodos());
            model.addAttribute("activePage", "citas");
            return "formulario-cita";
        }
        citaService.guardar(cita);
        return "redirect:/citas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cita", citaService.obtenerPorId(id));
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("personal", personalService.listarTodos());
        model.addAttribute("activePage", "citas");
        return "formulario-cita";
    }

    @GetMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id) {
        citaService.cancelar(id);
        return "redirect:/citas";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        citaService.eliminar(id);
        return "redirect:/citas";
    }
}
