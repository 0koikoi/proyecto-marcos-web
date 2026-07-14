package pe.edu.utp.huellitas.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.format.annotation.DateTimeFormat;

import pe.edu.utp.huellitas.model.Cita;
import pe.edu.utp.huellitas.service.CitaService;
import pe.edu.utp.huellitas.service.PacienteService;
import pe.edu.utp.huellitas.service.PersonalService;

/**
 * Controller de gestión de citas.
 *
 * Permisos:
 * - Ver lista y detalle: todos los roles autenticados
 * - Crear / Editar: ADMINISTRADOR + RECEPCION
 * - Cancelar: ADMINISTRADOR + RECEPCION
 * - Eliminar: solo ADMINISTRADOR
 */
@Controller
@RequestMapping("/citas")
public class CitaController {

    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final PersonalService personalService;

    public CitaController(CitaService citaService,
            PacienteService pacienteService,
            PersonalService personalService) {
        this.citaService = citaService;
        this.pacienteService = pacienteService;
        this.personalService = personalService;
    }

    // ── Formateador de Fecha ──────────────────────────────────────────────────

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(OffsetDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text != null && !text.isBlank()) {
                    LocalDateTime ldt = LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    setValue(ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime());
                } else {
                    setValue(null);
                }
            }
        });
    }

    // ── Ver lista ─────────────────────────────────────────────────────────────

    @GetMapping
    public String listar(@RequestParam(required = false) String dni,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        OffsetDateTime start = null;
        OffsetDateTime end = null;

        if (fechaInicio != null) {
            start = fechaInicio.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        }
        if (fechaFin != null) {
            end = fechaFin.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }

        model.addAttribute("citas", citaService.buscarPorFiltros(dni, start, end));
        model.addAttribute("paramDni", dni);
        model.addAttribute("paramFechaInicio", fechaInicio);
        model.addAttribute("paramFechaFin", fechaFin);
        model.addAttribute("activePage", "citas");
        return "citas";
    }

    // ── Formulario nueva cita ─────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cita", new Cita());
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("personal", personalService.listarVeterinarios());
        model.addAttribute("activePage", "citas");
        return "formulario-cita";
    }

    // ── Guardar cita ──────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cita") Cita cita,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("pacientes", pacienteService.listarTodos(null));
            model.addAttribute("personal", personalService.listarVeterinarios());
            model.addAttribute("activePage", "citas");
            return "formulario-cita";
        }

        try {
            if (authentication != null && authentication.getName() != null) {
                personalService.obtenerPorUsername(authentication.getName())
                        .ifPresent(cita::setCreatedBy);
            }
            citaService.guardar(cita);
            redirectAttrs.addFlashAttribute("successMsg", "Cita registrada correctamente.");
            return "redirect:/citas";
        } catch (Exception e) {
            model.addAttribute("pacientes", pacienteService.listarTodos(null));
            model.addAttribute("personal", personalService.listarVeterinarios());
            model.addAttribute("activePage", "citas");
            model.addAttribute("error", "No se pudo registrar la cita: " + e.getMessage());
            return "formulario-cita";
        }
    }

    // ── Formulario editar cita ────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            model.addAttribute("cita", citaService.obtenerPorId(id));
            model.addAttribute("pacientes", pacienteService.listarTodos(null));
            model.addAttribute("personal", personalService.listarVeterinarios());
            model.addAttribute("activePage", "citas");
            return "formulario-cita";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se encontró la cita solicitada.");
            return "redirect:/citas";
        }
    }

    // ── Cancelar cita (ADMIN + RECEPCION + VETERINARIO) ──────────────────────
    // IMPORTANTE: debe ser POST — nunca usar GET para operaciones de escritura.

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            citaService.cancelar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Cita cancelada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se pudo cancelar la cita: " + e.getMessage());
        }
        return "redirect:/citas";
    }

    // ── Iniciar cita (ADMIN + RECEPCION + VETERINARIO) ──────────────────────
    // IMPORTANTE: debe ser POST — nunca usar GET para operaciones de escritura.

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @PostMapping("/iniciar/{id}")
    public String iniciar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            citaService.iniciar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Cita marcada como en proceso.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se pudo iniciar la cita: " + e.getMessage());
        }
        return "redirect:/citas";
    }

    // ── Completar cita (ADMIN + RECEPCION + VETERINARIO) ─────────────────────
    // IMPORTANTE: debe ser POST — nunca usar GET para operaciones de escritura.

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @PostMapping("/completar/{id}")
    public String completar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            citaService.completar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Cita marcada como completada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se pudo completar la cita: " + e.getMessage());
        }
        return "redirect:/citas";
    }

    // ── Eliminar cita (solo ADMINISTRADOR) ───────────────────────────────────
    // IMPORTANTE: debe ser POST — nunca usar GET para operaciones de escritura.

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            citaService.eliminar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Cita eliminada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg",
                    "No se pudo eliminar: la cita tiene registros clínicos asociados.");
        }
        return "redirect:/citas";
    }
}
