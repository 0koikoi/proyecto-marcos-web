package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.repository.PersonalRepository;

@Controller
@RequestMapping("/personal")
public class PersonalController {

    private final PersonalRepository repository;
    //inyección de dependencias por constructor
    public PersonalController(PersonalRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String listarPersonal(Model model) {
        //bd de la lista
        model.addAttribute("listaPersonal", repository.findAll());
        //bd personal
        model.addAttribute("nuevoPersonal", new Personal());
        return "personal"; //personal.html
    }

    @PostMapping("/guardar")
    public String guardarPersonal(@ModelAttribute Personal personal, Model model) {
        //validación, el código debe ser C sumado a 6 dígitos
        if (!personal.getCodigoInstitucional().matches("^C\\d{6}$")) {
            model.addAttribute("errorCodigo", "El código debe empezar con 'C' seguido de exactamente 6 dígitos.");
            model.addAttribute("listaPersonal", repository.findAll());
            model.addAttribute("nuevoPersonal", personal);
            return "personal";
        }

        repository.save(personal);
        return "redirect:/personal";
    }
}