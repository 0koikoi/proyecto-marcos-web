package pe.edu.utp.huellitas.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.model.Paciente;
import pe.edu.utp.huellitas.model.Vacuna;
import pe.edu.utp.huellitas.service.PacienteService;
import pe.edu.utp.huellitas.service.PersonalService;
import pe.edu.utp.huellitas.service.VacunaService;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller de Vacunas.
 *
 * Referencia de permisos:
 *   ADMINISTRADOR → acceso completo
 *   VETERINARIO   → crear y ver vacunas
 *   RECEPCION     → SIN acceso a este módulo
 */
@Controller
@RequestMapping("/vacunas")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
public class VacunaController {

    private final VacunaService vacunaService;
    private final PacienteService pacienteService;
    private final PersonalService personalService;

    public VacunaController(VacunaService vacunaService,
                            PacienteService pacienteService,
                            PersonalService personalService) {
        this.vacunaService = vacunaService;
        this.pacienteService = pacienteService;
        this.personalService = personalService;
    }

    // ── Lista general ─────────────────────────────────────────────────────────

    /** Días de anticipación para resaltar la próxima dosis en la lista. */
    private static final int DIAS_ALERTA_PROXIMA_DOSIS = 7;

    /**
     * Lista todas las vacunas, o las de un paciente si se indica pacienteId.
     * Incluye fechaLimite para resaltar en la vista las próximas dosis (< 7 días).
     */
    @GetMapping
    public String listar(@RequestParam(required = false) Long pacienteId, Model model) {
        List<Vacuna> vacunas = (pacienteId != null)
                ? vacunaService.listarPorPaciente(pacienteId)
                : vacunaService.listarTodas();
        model.addAttribute("vacunas", vacunas);
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("pacienteIdSeleccionado", pacienteId);
        model.addAttribute("fechaLimite", LocalDate.now().plusDays(DIAS_ALERTA_PROXIMA_DOSIS));
        model.addAttribute("hayProximas", !vacunaService.listarProximasDosis(DIAS_ALERTA_PROXIMA_DOSIS).isEmpty());
        model.addAttribute("activePage", "vacunas");
        return "vacunas/lista";
    }

    // ── Vacunas por paciente ──────────────────────────────────────────────────

    @GetMapping("/paciente/{pacienteId}")
    public String listarPorPaciente(@PathVariable Long pacienteId, Model model,
                                     RedirectAttributes redirectAttrs) {
        try {
            model.addAttribute("vacunas", vacunaService.listarPorPaciente(pacienteId));
            model.addAttribute("paciente", pacienteService.obtenerPorId(pacienteId));
            model.addAttribute("activePage", "vacunas");
            return "vacunas/lista";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se encontró el paciente.");
            return "redirect:/pacientes";
        }
    }

    // ── Formulario nueva vacuna ───────────────────────────────────────────────

    /**
     * Muestra el formulario de nueva vacuna.
     * Acepta pacienteId opcional para pre-seleccionar el paciente.
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long pacienteId, Model model) {
        Vacuna vacuna = new Vacuna();
        if (pacienteId != null) {
            Paciente paciente = new Paciente();
            paciente.setId(pacienteId);
            vacuna.setPaciente(paciente);
        }
        model.addAttribute("vacuna", vacuna);
        cargarFormulario(model);
        return "vacunas/formulario";
    }

    // ── Guardar vacuna ────────────────────────────────────────────────────────

    /** Registra una nueva vacuna aplicada a un paciente. */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("vacuna") Vacuna vacuna,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            cargarFormulario(model);
            return "vacunas/formulario";
        }
        try {
            vacunaService.guardar(vacuna);
            redirectAttrs.addFlashAttribute("successMsg", "Vacuna registrada correctamente.");
            return "redirect:/vacunas";
        } catch (IllegalArgumentException e) {
            cargarFormulario(model);
            model.addAttribute("error", e.getMessage());
            return "vacunas/formulario";
        }
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private void cargarFormulario(Model model) {
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("personal", personalService.listarTodos());
        model.addAttribute("activePage", "vacunas");
    }

    // ── Eliminar (solo ADMIN) ─────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            vacunaService.eliminar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Registro de vacuna eliminado.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/vacunas";
    }
}
