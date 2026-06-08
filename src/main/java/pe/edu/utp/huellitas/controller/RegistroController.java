package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.repository.PersonalRepository;

@Controller
public class RegistroController {

    private final PersonalRepository repository;

    public RegistroController(
            PersonalRepository repository) {

        this.repository = repository;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {

        model.addAttribute(
                "usuario",
                new Personal());

        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @ModelAttribute Personal usuario) {

        usuario.setActivo(true);

        repository.save(usuario);

        return "redirect:/";
    }
}