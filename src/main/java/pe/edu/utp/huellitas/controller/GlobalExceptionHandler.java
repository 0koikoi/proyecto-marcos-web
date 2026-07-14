package pe.edu.utp.huellitas.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        model.addAttribute("errorGlobal", ex.getMessage());
        return "error-generico"; //crea un html sencillito en templates llamado error-generico.html
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex, Model model) {
        // Podría ser un duplicado de un valor único (como DNI, usuario) o restricción de clave foránea.
        model.addAttribute("errorGlobal", "La operación no se pudo completar porque entra en conflicto con datos existentes (ej. el registro ya existe o está asociado a otros datos).");
        return "error-generico";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntime(RuntimeException ex, Model model) {
        model.addAttribute("errorGlobal", ex.getMessage());
        return "error-generico";
    }
}