package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        return "citas"; // Asegúrate de que Aaron haga el archivo citas.html
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("personal", personalService.listarTodos());
        return "formulario-cita"; // Aaron debe crear este HTML con los <select>
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Cita cita) {
        citaService.guardar(cita);
        return "redirect:/citas";
    }
}
