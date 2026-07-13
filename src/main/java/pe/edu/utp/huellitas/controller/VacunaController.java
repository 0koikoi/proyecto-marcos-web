package pe.edu.utp.huellitas.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.service.PacienteService;
import pe.edu.utp.huellitas.service.PersonalService;
import pe.edu.utp.huellitas.service.VacunaService;

/**
 * Controller de Vacunas.
 *
 * ════════════════════════════════════════════════════════════
 * TODO — MÓDULO A IMPLEMENTAR POR EL EQUIPO
 * ════════════════════════════════════════════════════════════
 *
 * La estructura base está lista. El desarrollador asignado debe:
 *
 *   1. Implementar el cuerpo de cada método.
 *
 *   2. Crear los templates en src/main/resources/templates/vacunas/:
 *      - lista.html      → tabla de vacunas con columna de próxima dosis
 *                          (resaltar en AMARILLO si la próxima dosis es en < 7 días)
 *      - formulario.html → formulario de registro de vacuna
 *
 *   3. En el formulario de nueva vacuna, incluir:
 *      - Select de paciente
 *      - Nombre de vacuna, laboratorio, lote
 *      - Fecha de aplicación (date picker)
 *      - Fecha de próxima dosis (opcional, date picker)
 *      - Observaciones
 *
 *   4. Integrar en el dashboard el conteo de vacunas próximas:
 *      Agregar al WebController un atributo "vacunasProximas"
 *      usando vacunaService.listarProximasDosis(7).
 *
 * Referencia de permisos:
 *   ADMINISTRADOR → acceso completo
 *   VETERINARIO   → crear y ver vacunas
 *   RECEPCION     → SIN acceso a este módulo
 * ════════════════════════════════════════════════════════════
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

    /**
     * Lista todas las vacunas.
     * TODO: Incluir filtro por paciente usando @RequestParam opcional.
     * TODO: Pasar al modelo también listarProximasDosis(7) para mostrar alertas.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("vacunas", vacunaService.listarTodas());
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
     * TODO: Recibir pacienteId como parámetro opcional para pre-seleccionar el paciente.
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long pacienteId, Model model) {
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("personal", personalService.listarTodos());
        model.addAttribute("activePage", "vacunas");
        return "vacunas/formulario";
    }

    // ── Guardar vacuna ────────────────────────────────────────────────────────

    /**
     * TODO: Recibir una Vacuna (o VacunaDTO) validada con @Valid.
     * TODO: Obtener el veterinario desde el SecurityContext.
     */
    @PostMapping("/guardar")
    public String guardar(RedirectAttributes redirectAttrs) {
        // TODO: Implementar este método
        redirectAttrs.addFlashAttribute("infoMsg",
                "Módulo en construcción. Implementar el formulario de vacunación.");
        return "redirect:/vacunas";
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
