package pe.edu.utp.huellitas.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.service.HistoriaClinicaService;
import pe.edu.utp.huellitas.service.PersonalService;
import pe.edu.utp.huellitas.service.RecetaService;

/**
 * Controller de Recetas Médicas.
 *
 * ════════════════════════════════════════════════════════════
 * TODO — MÓDULO A IMPLEMENTAR POR EL EQUIPO
 * ════════════════════════════════════════════════════════════
 *
 * La estructura base está lista. El desarrollador asignado debe:
 *
 *   1. Implementar el cuerpo de cada método.
 *
 *   2. Crear los templates en src/main/resources/templates/recetas/:
 *      - lista.html      → recetas de una historia clínica
 *      - formulario.html → crear receta con múltiples medicamentos
 *                          (requiere JavaScript para agregar/quitar filas)
 *      - detalle.html    → vista de impresión de la receta
 *
 *   3. El formulario debe manejar una lista dinámica de DetalleReceta:
 *      - Cada línea: medicamento, presentación, dosis, frecuencia,
 *        duración (días), cantidad, observaciones
 *      - Botón "Agregar medicamento" (JS) y "Quitar" por fila
 *
 *   4. La vista de detalle (/recetas/{id}) debe estar diseñada para
 *      imprimirse como documento médico (usar @media print en CSS).
 *
 * Referencia de permisos:
 *   ADMINISTRADOR → acceso completo
 *   VETERINARIO   → crear y ver recetas
 *   RECEPCION     → SIN acceso a este módulo
 * ════════════════════════════════════════════════════════════
 */
@Controller
@RequestMapping("/recetas")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
public class RecetaController {

    private final RecetaService recetaService;
    private final HistoriaClinicaService historiaClinicaService;
    private final PersonalService personalService;

    public RecetaController(RecetaService recetaService,
                            HistoriaClinicaService historiaClinicaService,
                            PersonalService personalService) {
        this.recetaService = recetaService;
        this.historiaClinicaService = historiaClinicaService;
        this.personalService = personalService;
    }

    // ── Recetas por historia clínica ──────────────────────────────────────────

    @GetMapping("/historia/{historiaId}")
    public String listarPorHistoria(@PathVariable Long historiaId, Model model,
                                     RedirectAttributes redirectAttrs) {
        try {
            model.addAttribute("recetas", recetaService.listarPorHistoria(historiaId));
            model.addAttribute("historia", historiaClinicaService.obtenerPorId(historiaId));
            model.addAttribute("activePage", "recetas");
            return "recetas/lista";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se encontró la historia clínica.");
            return "redirect:/historia";
        }
    }

    // ── Formulario nueva receta ───────────────────────────────────────────────

    /**
     * TODO: Recibir historiaId como parámetro para pre-vincular la receta.
     * TODO: Crear RecetaDTO que soporte lista de DetalleRecetaDTO.
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long historiaId, Model model) {
        model.addAttribute("historias", historiaClinicaService.listarTodas());
        model.addAttribute("personal", personalService.listarTodos());
        model.addAttribute("activePage", "recetas");
        return "recetas/formulario";
    }

    // ── Guardar receta ────────────────────────────────────────────────────────

    /**
     * TODO: Implementar binding del formulario con lista de detalles.
     * La entidad Receta ya tiene @OneToMany(cascade=ALL) para DetalleReceta.
     */
    @PostMapping("/guardar")
    public String guardar(RedirectAttributes redirectAttrs) {
        // TODO: Implementar este método
        redirectAttrs.addFlashAttribute("infoMsg",
                "Módulo en construcción. Implementar el formulario de recetas.");
        return "redirect:/recetas/nueva";
    }

    // ── Detalle / impresión ───────────────────────────────────────────────────

    /**
     * Vista de detalle de la receta, diseñada para impresión.
     * TODO: Agregar un botón "Imprimir" que llame a window.print() en JS.
     */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            model.addAttribute("receta", recetaService.obtenerPorId(id));
            model.addAttribute("activePage", "recetas");
            return "recetas/detalle";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se encontró la receta.");
            return "redirect:/historia";
        }
    }

    // ── Eliminar (solo ADMIN) ─────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            recetaService.eliminar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Receta eliminada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/historia";
    }
}
