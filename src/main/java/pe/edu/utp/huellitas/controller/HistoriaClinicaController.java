package pe.edu.utp.huellitas.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.model.Cita;
import pe.edu.utp.huellitas.model.EstadoCita;
import pe.edu.utp.huellitas.model.HistoriaClinica;
import pe.edu.utp.huellitas.model.Paciente;
import pe.edu.utp.huellitas.service.CitaService;
import pe.edu.utp.huellitas.service.HistoriaClinicaService;
import pe.edu.utp.huellitas.service.PacienteService;
import pe.edu.utp.huellitas.service.PersonalService;
import pe.edu.utp.huellitas.service.RecetaService;
import pe.edu.utp.huellitas.service.VacunaService;

import java.time.LocalDate;

/**
 * Controller de Historia Clínica.
 *
 * Referencia de permisos:
 *   ADMINISTRADOR → acceso completo
 *   VETERINARIO   → crear y ver historias (solo de sus pacientes en producción)
 *   RECEPCION     → SIN acceso a este módulo
 */
@Controller
@RequestMapping("/historia")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;
    private final PacienteService pacienteService;
    private final PersonalService personalService;
    private final RecetaService recetaService;
    private final VacunaService vacunaService;
    private final CitaService citaService;

    public HistoriaClinicaController(HistoriaClinicaService historiaClinicaService,
                                     PacienteService pacienteService,
                                     PersonalService personalService,
                                     RecetaService recetaService,
                                     VacunaService vacunaService,
                                     CitaService citaService) {
        this.historiaClinicaService = historiaClinicaService;
        this.pacienteService = pacienteService;
        this.personalService = personalService;
        this.recetaService = recetaService;
        this.vacunaService = vacunaService;
        this.citaService = citaService;
    }

    // ── Lista general ─────────────────────────────────────────────────────────

    /**
     * Muestra la lista de historias clínicas.
     * Soporta búsqueda por nombre de paciente/propietario y filtro por rango de fecha.
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String buscar,
                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate hasta,
                         Model model) {
        boolean hayFiltros = (buscar != null && !buscar.isBlank()) || desde != null || hasta != null;
        model.addAttribute("historias", hayFiltros
                ? historiaClinicaService.buscar(buscar, desde, hasta)
                : historiaClinicaService.listarTodas());
        model.addAttribute("buscar", buscar);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("activePage", "historia");
        return "historia/lista";
    }

    // ── Historia por paciente ─────────────────────────────────────────────────

    /**
     * Muestra el historial clínico de un paciente específico.
     * Útil para la ficha del paciente.
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
     * Acepta pacienteId y citaId opcionales para pre-seleccionar esos campos.
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long pacienteId,
                        @RequestParam(required = false) Long citaId,
                        Model model) {
        HistoriaClinica historia = new HistoriaClinica();
        if (pacienteId != null) {
            Paciente paciente = new Paciente();
            paciente.setId(pacienteId);
            historia.setPaciente(paciente);
        }
        if (citaId != null) {
            Cita cita = new Cita();
            cita.setId(citaId);
            historia.setCita(cita);
        }
        model.addAttribute("historia", historia);
        cargarFormulario(model);
        return "historia/formulario";
    }

    // ── Guardar historia ──────────────────────────────────────────────────────

    /**
     * Guarda una nueva entrada de historia clínica.
     * Si viene vinculada a una cita, esa cita se marca como COMPLETADA.
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("historia") HistoriaClinica historia,
                          BindingResult result,
                          @RequestParam(required = false) Long citaId,
                          Model model,
                          RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            cargarFormulario(model);
            return "historia/formulario";
        }
        try {
            if (citaId != null) {
                Cita cita = citaService.obtenerPorId(citaId);
                if (!cita.getPaciente().getId().equals(historia.getPaciente().getId())) {
                    throw new IllegalArgumentException("El paciente de la historia clínica no coincide con el paciente de la cita seleccionada.");
                }
                historia.setCita(cita);
            } else {
                historia.setCita(null);
            }
            HistoriaClinica guardada = historiaClinicaService.guardar(historia);
            if (guardada.getCita() != null) {
                citaService.completar(guardada.getCita().getId());
            }
            redirectAttrs.addFlashAttribute("successMsg", "Consulta registrada correctamente.");
            return "redirect:/historia/" + guardada.getId();
        } catch (RuntimeException e) {
            cargarFormulario(model);
            model.addAttribute("error", e.getMessage());
            return "historia/formulario";
        }
    }

    // ── Métodos privados ──────────────────────────────────────────────────────

    private void cargarFormulario(Model model) {
        model.addAttribute("pacientes", pacienteService.listarTodos(null));
        model.addAttribute("personal", personalService.listarVeterinarios());
        model.addAttribute("citas", citaService.listarTodas().stream()
                .filter(c -> c.getEstado() == EstadoCita.PENDIENTE || c.getEstado() == EstadoCita.EN_PROCESO)
                .toList());
        model.addAttribute("activePage", "historia");
    }

    // ── Detalle de historia ───────────────────────────────────────────────────

    /**
     * Vista de detalle de una consulta.
     * Incluye en el modelo: la historia, sus recetas y las vacunas del paciente.
     */
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            var historia = historiaClinicaService.obtenerPorId(id);
            model.addAttribute("historia", historia);
            model.addAttribute("recetas", recetaService.listarPorHistoria(historia.getId()));
            model.addAttribute("vacunas", vacunaService.listarPorPaciente(historia.getPaciente().getId()));
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
