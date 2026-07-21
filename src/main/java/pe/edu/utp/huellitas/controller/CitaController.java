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
import org.springframework.format.annotation.DateTimeFormat;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import pe.edu.utp.huellitas.model.Cita;
import pe.edu.utp.huellitas.service.CitaService;
import pe.edu.utp.huellitas.service.PacienteService;
import pe.edu.utp.huellitas.service.PersonalService;

/**
 * Controller de gestión de citas.
 *
 * Permisos por endpoint:
 *   - Listar/Ver:    todos los roles autenticados
 *   - Crear/Editar:  ADMINISTRADOR, RECEPCION, VETERINARIO
 *   - Cancelar/Completar/Iniciar: ADMINISTRADOR, RECEPCION, VETERINARIO
 *   - Eliminar:      solo ADMINISTRADOR
 *
 * Manejo de errores: todas las excepciones de negocio (NegocioException)
 * son capturadas por GlobalExceptionHandler. No se usan try/catch locales.
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

    // ── Conversión de fechas HTML → OffsetDateTime ────────────────────────────

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

    // ── Listar citas (todos los roles autenticados) ───────────────────────────

    @GetMapping
    public String listar(@RequestParam(required = false) String dni,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                         Model model) {
        OffsetDateTime start = fechaInicio != null
                ? fechaInicio.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime() : null;
        OffsetDateTime end = fechaFin != null
                ? fechaFin.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toOffsetDateTime() : null;

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
        cargarFormulario(model);
        return "formulario-cita";
    }

    // ── Guardar cita (crear o actualizar) ─────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cita") Cita cita,
                          BindingResult result,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            cargarFormulario(model);
            return "formulario-cita";
        }

        if (authentication != null && authentication.getName() != null) {
            personalService.obtenerPorUsername(authentication.getName())
                    .ifPresent(cita::setCreadoPor);
        }

        citaService.guardar(cita);
        redirectAttrs.addFlashAttribute("successMsg", "Cita registrada correctamente.");
        return "redirect:/citas";
    }

    // ── Formulario editar cita ────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cita", citaService.obtenerPorId(id));
        cargarFormulario(model);
        return "formulario-cita";
    }

    // ── Cancelar cita ─────────────────────────────────────────────────────────
    // POST — nunca GET para operaciones de escritura.

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        citaService.cancelar(id);
        redirectAttrs.addFlashAttribute("successMsg", "Cita cancelada correctamente.");
        return "redirect:/citas";
    }

    // ── Iniciar cita ──────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @PostMapping("/iniciar/{id}")
    public String iniciar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        citaService.iniciar(id);
        redirectAttrs.addFlashAttribute("successMsg", "Cita marcada como en proceso.");
        return "redirect:/citas";
    }

    // ── Completar cita ────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','RECEPCION','VETERINARIO')")
    @PostMapping("/completar/{id}")
    public String completar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        citaService.completar(id);
        redirectAttrs.addFlashAttribute("successMsg", "Cita marcada como completada.");
        return "redirect:/citas";
    }

    // ── Eliminar cita (solo ADMINISTRADOR) ───────────────────────────────────
    // POST — nunca GET para operaciones de escritura.

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        citaService.eliminar(id);
        redirectAttrs.addFlashAttribute("successMsg", "Cita eliminada correctamente.");
        return "redirect:/citas";
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private void cargarFormulario(Model model) {
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("personal", personalService.listarVeterinarios());
        model.addAttribute("activePage", "citas");
    }
}
