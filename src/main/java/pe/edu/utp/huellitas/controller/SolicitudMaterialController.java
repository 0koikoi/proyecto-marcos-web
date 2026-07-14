package pe.edu.utp.huellitas.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.model.SolicitudMaterial;
import pe.edu.utp.huellitas.service.PersonalService;
import pe.edu.utp.huellitas.service.ProductoService;
import pe.edu.utp.huellitas.service.SolicitudMaterialService;

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

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("productos", productoService.listar());
        model.addAttribute("activePage", "solicitudes");
        return "solicitudes/formulario";
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VETERINARIO')")
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute SolicitudMaterial solicitud,
            Authentication authentication,
            RedirectAttributes redirectAttrs) {
        try {
            Personal usuarioActual = (Personal) authentication.getPrincipal();
            solicitud.setSolicitante(usuarioActual);
            solicitudService.guardar(solicitud);
            redirectAttrs.addFlashAttribute("successMsg", "Solicitud de material enviada correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "Error al crear la solicitud: " + e.getMessage());
        }
        return "redirect:/solicitudes";
    }

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