package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador de la página de login.
 *
 * Spring Security maneja por completo el POST /login (autenticación)
 * y el POST/GET /logout. Aquí solo renderizamos las vistas con
 * mensajes de error o confirmación.
 */
@Controller
public class LoginController {

    /** Muestra el formulario de login. */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error",   required = false) String error,
            @RequestParam(value = "logout",  required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "Usuario o contraseña incorrectos. Verifique sus credenciales.");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "Sesión cerrada correctamente.");
        }
        if (expired != null) {
            model.addAttribute("errorMsg", "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.");
        }

        return "login";
    }

    /** Redirección de la raíz al login. */
    @GetMapping("/")
    public String inicio() {
        return "redirect:/login";
    }
}
