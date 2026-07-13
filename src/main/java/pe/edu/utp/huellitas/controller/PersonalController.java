package pe.edu.utp.huellitas.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.service.PersonalService;

/**
 * CRUD completo de gestión de personal.
 * Accesible únicamente para el rol ADMINISTRADOR.
 *
 * Nota: Este controller NO inyecta RolRepository directamente.
 * Usa PersonalService.listarRoles() para obtener el catálogo de roles.
 */
@Controller
@RequestMapping("/personal")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class PersonalController {

    private final PersonalService service;

    public PersonalController(PersonalService service) {
        this.service = service;
    }

    // ── Listar ────────────────────────────────────────────────────────────────

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("listaPersonal", service.listarTodos());
        model.addAttribute("nuevoPersonal", new Personal());
        model.addAttribute("roles", service.listarRoles());
        model.addAttribute("editando", false);
        model.addAttribute("activePage", "personal");
        return "personal";
    }

    // ── Formulario de edición ─────────────────────────────────────────────────

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        return service.obtenerPorId(id).map(persona -> {
            model.addAttribute("nuevoPersonal", persona);
            model.addAttribute("listaPersonal", service.listarTodos());
            model.addAttribute("roles",         service.listarRoles());
            model.addAttribute("editando",      true);
            model.addAttribute("activePage",    "personal");
            return "personal";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("errorMsg", "No se encontró el miembro del personal.");
            return "redirect:/personal";
        });
    }

    // ── Guardar (crear o actualizar) ──────────────────────────────────────────

    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("nuevoPersonal") Personal personal,
            BindingResult result,
            @RequestParam(value = "rawPassword", required = false) String rawPassword,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (result.hasErrors()) {
            model.addAttribute("listaPersonal", service.listarTodos());
            model.addAttribute("roles",         service.listarRoles());
            model.addAttribute("editando",      personal.getId() != null);
            model.addAttribute("activePage",    "personal");
            return "personal";
        }

        boolean esEdicion = personal.getId() != null;
        String error = service.guardar(personal, rawPassword);
        if (error != null) {
            model.addAttribute("errorMsg",      error);
            model.addAttribute("listaPersonal", service.listarTodos());
            model.addAttribute("roles",         service.listarRoles());
            model.addAttribute("editando",      esEdicion);
            model.addAttribute("activePage",    "personal");
            return "personal";
        }

    return "redirect:/personal";
}
}