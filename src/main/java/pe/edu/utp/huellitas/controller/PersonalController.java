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
import pe.edu.utp.huellitas.dto.PersonalDTO;
import pe.edu.utp.huellitas.service.PersonalService;

//crud para gestion de personal

@Controller
@RequestMapping("/personal")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class PersonalController {

    private final PersonalService service;

    public PersonalController(PersonalService service) {
        this.service = service;
    }

    // listar
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
            dto.setNombre(persona.getNombre());
            dto.setApellido(persona.getApellido());
            dto.setRol(persona.getRol());
            dto.setTelefono(persona.getTelefono());
            dto.setEmail(persona.getEmail());
            dto.setUsername(persona.getUsername());

            model.addAttribute("nuevoPersonal", dto);
            model.addAttribute("listaPersonal", service.listarTodos());
            model.addAttribute("roles", service.listarRoles());
            model.addAttribute("editando", true);
            model.addAttribute("activePage", "personal");
            return "personal";
        }).orElseGet(() -> {
            redirectAttrs.addFlashAttribute("errorMsg", "No se encontró el miembro del personal.");
            return "redirect:/personal";
        });
    }

    // guarda
    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("nuevoPersonal") PersonalDTO dto,
            BindingResult result,
            @RequestParam(value = "rawPassword", required = false) String rawPassword,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (result.hasErrors()) {
            model.addAttribute("listaPersonal", service.listarTodos());
            model.addAttribute("roles", service.listarRoles());
            model.addAttribute("editando", dto.getId() != null);
            model.addAttribute("activePage", "personal");
            return "personal";
        }

        boolean esEdicion = dto.getId() != null;
        Personal personal = esEdicion ? service.obtenerPorId(dto.getId()).orElse(new Personal()) : new Personal();

        personal.setCodigoInstitucional(dto.getCodigoInstitucional());
        personal.setNombre(dto.getNombre());
        personal.setApellido(dto.getApellido());
        personal.setRol(dto.getRol());
        personal.setTelefono(dto.getTelefono());
        personal.setEmail(dto.getEmail());
        personal.setUsername(dto.getUsername());
        String passwordTemporal = null;
        if (!esEdicion) {
            passwordTemporal = service.generarPasswordTemporal();
            rawPassword = passwordTemporal;
        }

        String error = service.guardar(personal, rawPassword);
        if (error != null) {
            model.addAttribute("errorMsg", error);
            model.addAttribute("listaPersonal", service.listarTodos());
            model.addAttribute("roles", service.listarRoles());
            model.addAttribute("editando", esEdicion);
            model.addAttribute("activePage", "personal");
            return "personal";
        }

        if (passwordTemporal != null) {
            redirectAttrs.addFlashAttribute("successMsg", "Personal registrado exitosamente en el sistema.");
            redirectAttrs.addFlashAttribute("newUserUsername", personal.getUsername());
            redirectAttrs.addFlashAttribute("newUserTempPassword", passwordTemporal);
        } else {
            redirectAttrs.addFlashAttribute("successMsg", "Personal actualizado exitosamente.");
        }

        return "redirect:/personal";
    }

    // ── Acciones (Eliminar, Activar, Desactivar) ──────────────────────────────
    
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            service.eliminar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Personal eliminado permanentemente.");
        } catch (pe.edu.utp.huellitas.exception.NegocioException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se pudo eliminar el personal porque tiene registros asociados. Considérelo desactivarlo en lugar de eliminarlo.");
        }
        return "redirect:/personal";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        service.desactivar(id);
        redirectAttrs.addFlashAttribute("successMsg", "El acceso del personal ha sido desactivado.");
        return "redirect:/personal";
    }

    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        service.cambiarEstado(id, true);
        redirectAttrs.addFlashAttribute("successMsg", "El acceso del personal ha sido restaurado.");
        return "redirect:/personal";
    }
}
