package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
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
    public String guardarPersonal(
        @Valid @ModelAttribute("nuevoPersonal") Personal personal,
        BindingResult result,
        Model model) {

    if (result.hasErrors()) {
        model.addAttribute("listaPersonal", service.listarTodos());
        return "personal";
    }

    String error = service.guardar(personal);

    if (error != null) {
        model.addAttribute("errorCodigo", error);
        model.addAttribute("listaPersonal", service.listarTodos());
        return "personal";
    }

    return "redirect:/personal";
}
}