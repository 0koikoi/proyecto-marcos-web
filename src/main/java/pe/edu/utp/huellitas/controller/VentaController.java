package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.huellitas.model.EstadoVenta;
import pe.edu.utp.huellitas.model.Venta;
import pe.edu.utp.huellitas.service.PersonalService;
import pe.edu.utp.huellitas.service.ProductoService;
import pe.edu.utp.huellitas.service.PropietarioService;
import pe.edu.utp.huellitas.service.ServicioService;
import pe.edu.utp.huellitas.service.VentaService;

import java.util.List;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final ServicioService servicioService;
    private final PropietarioService propietarioService;
    private final PersonalService personalService;

    public VentaController(VentaService ventaService, ProductoService productoService, 
                           ServicioService servicioService,
                           PropietarioService propietarioService, PersonalService personalService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.servicioService = servicioService;
        this.propietarioService = propietarioService;
        this.personalService = personalService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoVenta estado, Model model) {
        model.addAttribute("ventas", ventaService.listarVentas(estado));
        model.addAttribute("estadoFiltro", estado);
        model.addAttribute("estados", EstadoVenta.values());
        return "ventas";
    }

    @GetMapping("/nuevo")
    public String formularioVenta(Model model) {
        model.addAttribute("venta", new Venta());
        model.addAttribute("productos", productoService.listar());
        model.addAttribute("servicios", servicioService.listarTodos());
        model.addAttribute("propietarios", propietarioService.listarTodos());
        model.addAttribute("personal", personalService.listarTodos());
        return "formulario-venta";
    }

    @PostMapping("/guardar")
    public String procesarVenta(@ModelAttribute Venta venta,
                                @RequestParam(value = "itemId[]") List<Long> itemIds,
                                @RequestParam(value = "tipoItem[]") List<String> tiposItem,
                                @RequestParam(value = "cantidad[]") List<Integer> cantidades,
                                @RequestParam(value = "metodoPago") String metodoPago,
                                RedirectAttributes redirectAttributes) {
        try {
            ventaService.registrarVentaMultilinea(venta, itemIds, tiposItem, cantidades, metodoPago);
            redirectAttributes.addFlashAttribute("successMsg", "Venta registrada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/ventas/nuevo";
        }
        return "redirect:/ventas";
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/{id}/anular")
    public String anularVenta(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ventaService.anularVenta(id);
            redirectAttributes.addFlashAttribute("successMsg", "Venta anulada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/ventas";
    }

    @GetMapping("/{id}/detalle")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("venta", ventaService.obtenerPorId(id));
        model.addAttribute("detalles", ventaService.obtenerDetallesPorVenta(id));
        return "detalle-venta";
    }
}