package pe.edu.utp.huellitas.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.service.PersonalService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PerfilController {

    private final PersonalService personalService;

    public PerfilController(PersonalService personalService) {
        this.personalService = personalService;
    }

    @GetMapping("/cambiar-password")
    public String mostrarFormularioCambioPassword(Model model, @AuthenticationPrincipal Personal personal) {
        if (personal == null) {
            return "redirect:/login";
        }
        model.addAttribute("obligatorio", personal.getDebeCambiarPassword());
        return "cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(
            @RequestParam("passwordActual") String passwordActual,
            @RequestParam("nuevaPassword") String nuevaPassword,
            @RequestParam("confirmarPassword") String confirmarPassword,
            @AuthenticationPrincipal Personal personal,
            RedirectAttributes redirectAttrs,
            HttpServletRequest request) {

        if (personal == null) {
            return "redirect:/login";
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            redirectAttrs.addFlashAttribute("errorMsg", "Las contraseñas nuevas no coinciden.");
            return "redirect:/cambiar-password";
        }

        String error = personalService.cambiarPassword(personal.getId(), passwordActual, nuevaPassword);
        
        if (error != null) {
            redirectAttrs.addFlashAttribute("errorMsg", error);
            return "redirect:/cambiar-password";
        }

        // Invalida la sesión para forzar que vuelva a loguearse con la nueva contraseña
        request.getSession().invalidate();
        return "redirect:/login?changed=true";
    }
}
