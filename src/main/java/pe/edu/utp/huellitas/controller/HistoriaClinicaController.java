package pe.edu.utp.huellitas.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.service.HistoriaClinicaService;
import pe.edu.utp.huellitas.service.PacienteService;
import pe.edu.utp.huellitas.service.PersonalService;

/**
 * Controller de Historia Clínica.
 *
 * ════════════════════════════════════════════════════════════
 * TODO — MÓDULO A IMPLEMENTAR POR EL EQUIPO
 * ════════════════════════════════════════════════════════════
 *
 * Este scaffold tiene las rutas base definidas y los servicios
 * inyectados. El desarrollador asignado debe:
 *
 *   1. Implementar el cuerpo de cada método (actualmente todos
 *      redirigen a /historia con un mensaje de "en construcción").
 *
 *   2. Crear los templates en src/main/resources/templates/historia/:
 *      - lista.html      → tabla de historias (filtrable por paciente)
 *      - formulario.html → formulario de nueva consulta
 *      - detalle.html    → vista completa de la consulta con recetas y vacunas
 *
 *   3. En el formulario de nueva historia, incluir:
 *      - Select de paciente (o recibir pacienteId por parámetro desde /pacientes)
 *      - Campos: motivoConsulta, diagnostico, tratamiento, observaciones
 *      - Campos opcionales: pesoKg, temperaturaC
 *      - Select de cita asociada (opcional — walk-in no tiene cita)
 *
 *   4. Al guardar una historia desde una cita, actualizar el estado
 *      de la cita a COMPLETADA.
 *
 *   5. Usar sec:authorize en los templates para ocultar botones
 *      de acción según el rol.
 *
 * Referencia de permisos:
 *   ADMINISTRADOR → acceso completo
 *   VETERINARIO   → crear y ver historias (solo de sus pacientes en producción)
 *   RECEPCION     → SIN acceso a este módulo
 * ════════════════════════════════════════════════════════════
 */
@Controller
@RequestMapping("/historia")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;
    private final PacienteService pacienteService;
    private final PersonalService personalService;

    public HistoriaClinicaController(HistoriaClinicaService historiaClinicaService,
                                     PacienteService pacienteService,
                                     PersonalService personalService) {
        this.historiaClinicaService = historiaClinicaService;
        this.pacienteService = pacienteService;
        this.personalService = personalService;
    }

    // ── Lista general ─────────────────────────────────────────────────────────

    /**
     * Muestra la lista de historias clínicas.
     * TODO: Implementar paginación y filtro por paciente.
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("historias", historiaClinicaService.listarTodas());
        model.addAttribute("activePage", "historia");
        return "historia/lista";
    }

    // ── Historia por paciente ─────────────────────────────────────────────────

    /**
     * Muestra el historial clínico de un paciente específico.
     * Útil para la ficha del paciente.
     * TODO: Pasar también los datos del paciente al modelo para mostrar cabecera.
     */
    @GetMapping("/paciente/{pacienteId}")
    public String listarPorPaciente(@PathVariable Long pacienteId, Model model,
                                     RedirectAttributes redirectAttrs) {
        try {
            model.addAttribute("historias",
                    historiaClinicaService.listarPorPaciente(pacienteId));
            model.addAttribute("paciente", pacienteService.obtenerPorId(pacienteId));
            model.addAttribute("activePage", "historia");
            return "historia/lista";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se encontró el paciente.");
            return "redirect:/pacientes";
        }
    }

    // ── Formulario nueva historia ─────────────────────────────────────────────

    /**
     * Muestra el formulario para registrar una nueva consulta.
     * TODO: Recibir pacienteId como parámetro opcional para pre-seleccionar el paciente.
     * TODO: Recibir citaId como parámetro opcional para vincular la consulta a una cita.
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long pacienteId,
                        @RequestParam(required = false) Long citaId,
                        Model model) {
        // TODO: Crear un HistoriaClinicaDTO y pre-cargar pacienteId y citaId si vienen
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("personal", personalService.listarTodos());
        model.addAttribute("activePage", "historia");
        return "historia/formulario";
    }

    // ── Guardar historia ──────────────────────────────────────────────────────

    /**
     * Guarda una nueva entrada de historia clínica.
     * TODO: Recibir un HistoriaClinicaDTO validado con @Valid.
     * TODO: Obtener el veterinario desde el SecurityContext (usuario autenticado).
     * TODO: Si viene de una cita (citaId != null), actualizar estado de la cita a COMPLETADA.
     */
    @PostMapping("/guardar")
    public String guardar(RedirectAttributes redirectAttrs) {
        // TODO: Implementar este método
        redirectAttrs.addFlashAttribute("infoMsg",
                "Módulo en construcción. Implementar el formulario de historia clínica.");
        return "redirect:/historia";
    }

    // ── Detalle de historia ───────────────────────────────────────────────────

    /**
     * Vista de detalle de una consulta.
     * TODO: Incluir en el modelo: la historia, sus recetas y las vacunas del paciente.
     */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            model.addAttribute("historia", historiaClinicaService.obtenerPorId(id));
            model.addAttribute("activePage", "historia");
            return "historia/detalle";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se encontró la historia clínica.");
            return "redirect:/historia";
        }
    }

    // ── Eliminar historia (solo ADMIN) ────────────────────────────────────────

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            historiaClinicaService.eliminar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Historia clínica eliminada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg",
                    "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/historia";
    }
}
