package pe.edu.utp.huellitas.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.service.PersonalService;
import pe.edu.utp.huellitas.service.ProductoService;
import pe.edu.utp.huellitas.service.SolicitudMaterialService;

/**
 * Controller de Solicitudes de Material.
 *
 * ════════════════════════════════════════════════════════════
 * TODO — MÓDULO A IMPLEMENTAR POR EL EQUIPO
 * ════════════════════════════════════════════════════════════
 *
 * La estructura base está lista. El desarrollador asignado debe:
 *
 *   1. Implementar el cuerpo de los métodos marcados con TODO.
 *
 *   2. Crear los templates en src/main/resources/templates/solicitudes/:
 *      - lista.html     → vista diferenciada según rol:
 *                         - ADMIN ve tabla con todas, con botones de acción
 *                         - VET ve solo las suyas, sin botones de acción
 *      - formulario.html → formulario simple: select producto, cantidad, motivo
 *
 *   3. En el template lista.html, usar sec:authorize para mostrar u ocultar:
 *      - Botón "Aprobar" → sec:authorize="hasRole('ADMINISTRADOR')"
 *      - Botón "Rechazar" → sec:authorize="hasRole('ADMINISTRADOR')"
 *      - Botón "Marcar entregada" → sec:authorize="hasRole('ADMINISTRADOR')"
 *
 *   4. El estado de cada solicitud debe mostrarse con un badge de color:
 *      PENDIENTE → amarillo | APROBADA → azul | ENTREGADA → verde | RECHAZADA → rojo
 *
 *   5. Al aprobar/rechazar, mostrar un modal con campo para observación.
 *
 *   6. Al marcar como entregada, pedir la cantidad entregada real.
 *
 * Referencia de permisos:
 *   ADMINISTRADOR → ver todas, aprobar, rechazar, entregar
 *   VETERINARIO   → crear solicitud, ver las propias
 *   RECEPCION     → SIN acceso a este módulo
 * ════════════════════════════════════════════════════════════
 */
@Controller
@RequestMapping("/solicitudes")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
public class SolicitudMaterialController {

    private final SolicitudMaterialService solicitudService;
    private final ProductoService productoService;
    private final PersonalService personalService;

    public SolicitudMaterialController(SolicitudMaterialService solicitudService,
                                       ProductoService productoService,
                                       PersonalService personalService) {
        this.solicitudService = solicitudService;
        this.productoService = productoService;
        this.personalService = personalService;
    }

    // ── Lista — diferenciada por rol ──────────────────────────────────────────

    /**
     * ADMIN: ve todas las solicitudes.
     * VETERINARIO: ve solo las propias.
     *
     * TODO: Detectar el rol del usuario autenticado usando Authentication
     *       y llamar al método correspondiente del servicio.
     *
     * Ejemplo de cómo obtener el usuario autenticado:
     *   Personal usuarioActual = (Personal) authentication.getPrincipal();
     */
    @GetMapping
    public String listar(Authentication authentication, Model model) {
        Personal usuarioActual = (Personal) authentication.getPrincipal();
        boolean esAdmin = usuarioActual.getRol().getNombre().equals("ADMINISTRADOR");

        if (esAdmin) {
            model.addAttribute("solicitudes", solicitudService.listarTodas());
            model.addAttribute("esAdmin", true);
        } else {
            model.addAttribute("solicitudes", solicitudService.listarPorSolicitante(usuarioActual.getId()));
            model.addAttribute("esAdmin", false);
        }
        model.addAttribute("activePage", "solicitudes");
        return "solicitudes/lista";
    }

    // ── Formulario nueva solicitud (solo VETERINARIO) ─────────────────────────

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("productos", productoService.listar());
        model.addAttribute("activePage", "solicitudes");
        return "solicitudes/formulario";
    }

    // ── Guardar solicitud ─────────────────────────────────────────────────────

    /**
     * TODO: Implementar el binding del formulario.
     * TODO: Obtener el solicitante desde el SecurityContext (usuario autenticado).
     *
     * Ejemplo de cómo obtener el usuario autenticado en un @PostMapping:
     *   @AuthenticationPrincipal Personal usuarioActual
     *   (requiere import org.springframework.security.core.annotation.AuthenticationPrincipal)
     */
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    @PostMapping("/guardar")
    public String guardar(RedirectAttributes redirectAttrs) {
        // TODO: Implementar este método
        redirectAttrs.addFlashAttribute("infoMsg",
                "Módulo en construcción. Implementar el formulario de solicitudes.");
        return "redirect:/solicitudes";
    }

    // ── Aprobar (solo ADMINISTRADOR) ──────────────────────────────────────────

    /**
     * TODO: Recibir observacion como @RequestParam del formulario modal.
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/aprobar/{id}")
    public String aprobar(@PathVariable Long id,
                          @RequestParam(required = false) String observacion,
                          Authentication authentication,
                          RedirectAttributes redirectAttrs) {
        try {
            Personal admin = (Personal) authentication.getPrincipal();
            solicitudService.aprobar(id, admin.getId(), observacion);
            redirectAttrs.addFlashAttribute("successMsg", "Solicitud aprobada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se pudo aprobar: " + e.getMessage());
        }
        return "redirect:/solicitudes";
    }

    // ── Rechazar (solo ADMINISTRADOR) ─────────────────────────────────────────

    /**
     * TODO: Recibir motivoRechazo como @RequestParam del formulario modal.
     *       El motivo de rechazo es obligatorio.
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/rechazar/{id}")
    public String rechazar(@PathVariable Long id,
                           @RequestParam String motivoRechazo,
                           Authentication authentication,
                           RedirectAttributes redirectAttrs) {
        try {
            Personal admin = (Personal) authentication.getPrincipal();
            solicitudService.rechazar(id, admin.getId(), motivoRechazo);
            redirectAttrs.addFlashAttribute("successMsg", "Solicitud rechazada.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se pudo rechazar: " + e.getMessage());
        }
        return "redirect:/solicitudes";
    }

    // ── Marcar como entregada (solo ADMINISTRADOR) ────────────────────────────

    /**
     * TODO: Recibir cantidadEntregada desde un modal con input numérico.
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/entregar/{id}")
    public String marcarEntregada(@PathVariable Long id,
                                  @RequestParam Integer cantidadEntregada,
                                  RedirectAttributes redirectAttrs) {
        try {
            solicitudService.marcarEntregada(id, cantidadEntregada);
            redirectAttrs.addFlashAttribute("successMsg", "Material marcado como entregado. Stock actualizado.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se pudo marcar como entregado: " + e.getMessage());
        }
        return "redirect:/solicitudes";
    }
}
