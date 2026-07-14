package pe.edu.utp.huellitas.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.dto.PersonalDTO;
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
        model.addAttribute("nuevoPersonal", new PersonalDTO());
        model.addAttribute("roles", service.listarRoles());
        model.addAttribute("editando", false);
        model.addAttribute("activePage", "personal");
        return "personal";
    }

    // ── Formulario de edición ─────────────────────────────────────────────────

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        return service.obtenerPorId(id).map(persona -> {
            PersonalDTO dto = new PersonalDTO();
            dto.setId(persona.getId());
            dto.setCodigoInstitucional(persona.getCodigoInstitucional());
            dto.setNombreCompleto(persona.getNombreCompleto());
            dto.setRol(persona.getRol());
            dto.setCargo(persona.getCargo());
            dto.setEspecialidad(persona.getEspecialidad());
            dto.setTelefono(persona.getTelefono());
            dto.setEmail(persona.getEmail());
            dto.setUsername(persona.getUsername());
            
            model.addAttribute("nuevoPersonal", dto);
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
            @Valid @ModelAttribute("nuevoPersonal") PersonalDTO dto,
            BindingResult result,
            @RequestParam(value = "rawPassword", required = false) String rawPassword,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (result.hasErrors()) {
            model.addAttribute("listaPersonal", service.listarTodos());
            model.addAttribute("roles",         service.listarRoles());
            model.addAttribute("editando",      dto.getId() != null);
            model.addAttribute("activePage",    "personal");
            return "personal";
        }

        boolean esEdicion = dto.getId() != null;
        Personal personal = esEdicion ? service.obtenerPorId(dto.getId()).orElse(new Personal()) : new Personal();
        
        personal.setCodigoInstitucional(dto.getCodigoInstitucional());
        personal.setNombreCompleto(dto.getNombreCompleto());
        personal.setRol(dto.getRol());
        personal.setCargo(dto.getCargo());
        personal.setEspecialidad(dto.getEspecialidad());
        personal.setTelefono(dto.getTelefono());
        personal.setEmail(dto.getEmail());
        personal.setUsername(dto.getUsername());

        String error = service.guardar(personal, rawPassword);
        if (error != null) {
            model.addAttribute("errorMsg",      error);
            model.addAttribute("listaPersonal", service.listarTodos());
            model.addAttribute("roles",         service.listarRoles());
            model.addAttribute("editando",      esEdicion);
            model.addAttribute("activePage",    "personal");
            return "personal";
        }

        redirectAttrs.addFlashAttribute("successMsg",
                esEdicion ? "Datos del personal actualizados correctamente."
                          : "Nuevo miembro registrado correctamente.");
        return "redirect:/personal";
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            service.eliminar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Miembro del personal eliminado.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg",
                    "No se pudo eliminar: el usuario tiene registros asociados en el sistema. " +
                    "Considere desactivarlo en su lugar.");
        }
        return "redirect:/personal";
    }

    // ── Activar / Desactivar ──────────────────────────────────────────────────

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        service.cambiarEstado(id, false);
        redirectAttrs.addFlashAttribute("successMsg", "Usuario desactivado correctamente.");
        return "redirect:/personal";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        service.cambiarEstado(id, true);
        redirectAttrs.addFlashAttribute("successMsg", "Usuario activado correctamente.");
        return "redirect:/personal";
    }
}