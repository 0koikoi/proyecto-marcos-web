package pe.edu.utp.huellitas.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.model.HistoriaClinica;
import pe.edu.utp.huellitas.model.Receta;
import pe.edu.utp.huellitas.service.HistoriaClinicaService;
import pe.edu.utp.huellitas.service.PersonalService;
import pe.edu.utp.huellitas.service.RecetaService;

import java.time.LocalDate;

/**
 * Controller de Recetas Médicas.
 *
 * Referencia de permisos:
 *   ADMINISTRADOR → acceso completo
 *   VETERINARIO   → crear y ver recetas
 *   RECEPCION     → SIN acceso a este módulo
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

    // ── Lista general ─────────────────────────────────────────────────────────

    /** Lista todas las recetas registradas (acceso directo desde el menú lateral). */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("recetas", recetaService.listarTodas());
        model.addAttribute("activePage", "recetas");
        return "recetas/lista";
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
     * Muestra el formulario de nueva receta.
     * Acepta historiaId opcional para pre-vincular la receta a una consulta.
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long historiaId, Model model) {
        Receta receta = new Receta();
        receta.setFechaEmision(LocalDate.now());
        if (historiaId != null) {
            HistoriaClinica historia = new HistoriaClinica();
            historia.setId(historiaId);
            receta.setHistoriaClinica(historia);
        }
        model.addAttribute("receta", receta);
        cargarFormulario(model);
        return "recetas/formulario";
    }

    // ── Guardar receta ────────────────────────────────────────────────────────

    /**
     * Guarda una receta junto con sus líneas de medicamento (DetalleReceta),
     * manejadas en cascada gracias a CascadeType.ALL en la entidad Receta.
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("receta") Receta receta,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            cargarFormulario(model);
            return "recetas/formulario";
        }
        try {
            Receta guardada = recetaService.guardar(receta);
            redirectAttrs.addFlashAttribute("successMsg", "Receta registrada correctamente.");
            return "redirect:/recetas/" + guardada.getId();
        } catch (IllegalArgumentException e) {
            cargarFormulario(model);
            model.addAttribute("error", e.getMessage());
            return "recetas/formulario";
        }
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private void cargarFormulario(Model model) {
        model.addAttribute("historias", historiaClinicaService.listarTodas());
        model.addAttribute("personal", personalService.listarVeterinarios());
        model.addAttribute("activePage", "recetas");
    }

    // ── Detalle / impresión ───────────────────────────────────────────────────

    /** Vista de detalle de la receta, diseñada para impresión. */
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
