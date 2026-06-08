package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.service.PersonalService;

@Controller
@RequestMapping("/personal")
public class PersonalController {

    private final PersonalService service;

    public PersonalController(PersonalService service) {
        this.service = service;
    }

    @GetMapping
    public String listarPersonal(Model model) {
        model.addAttribute("listaPersonal", service.listarTodos());
        model.addAttribute("nuevoPersonal", new Personal());
        return "personal";
    }

    @PostMapping("/guardar")
    public String guardarPersonal(@ModelAttribute Personal personal, Model model) {
        String error = service.guardar(personal);

        if (error != null) {
            model.addAttribute("errorCodigo", error);
            model.addAttribute("listaPersonal", service.listarTodos());
            model.addAttribute("nuevoPersonal", personal);
            return "personal";
        }
        return "redirect:/personal";
    }
}

