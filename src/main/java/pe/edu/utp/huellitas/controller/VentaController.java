package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.huellitas.model.Venta;
import pe.edu.utp.huellitas.service.PersonalService;
import pe.edu.utp.huellitas.service.ProductoService;
import pe.edu.utp.huellitas.service.PropietarioService;
import pe.edu.utp.huellitas.service.VentaService;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final PropietarioService propietarioService;
    private final PersonalService personalService;

    public VentaController(VentaService ventaService, ProductoService productoService, PropietarioService propietarioService, PersonalService personalService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
        this.propietarioService = propietarioService;
        this.personalService = personalService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", ventaService.listarVentas());
        return "ventas";
    }

    @GetMapping("/nuevo")
    public String formularioVenta(Model model) {
        model.addAttribute("venta", new Venta());
        model.addAttribute("productos", productoService.listar());
        model.addAttribute("propietarios", propietarioService.listarTodos());
        model.addAttribute("personal", personalService.listarTodos());
        return "formulario-venta";
    }

    @PostMapping("/guardar")
    public String procesarVenta(@ModelAttribute Venta venta, 
                                @RequestParam Long productoId, 
                                @RequestParam Integer cantidad) {
        ventaService.registrarVenta(venta, productoId, cantidad);
        return "redirect:/ventas";
    }
} 
