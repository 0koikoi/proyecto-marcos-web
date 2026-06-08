package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.utp.huellitas.repository.PacienteRepository;
import pe.edu.utp.huellitas.repository.CitaRepository;
import pe.edu.utp.huellitas.repository.ProductoRepository;

@Controller
public class WebController {

    private final PacienteRepository pacienteRepo;
    private final CitaRepository citaRepo;
    private final ProductoRepository productoRepo;

    public WebController(PacienteRepository pacienteRepo, CitaRepository citaRepo, ProductoRepository productoRepo) {
        this.pacienteRepo = pacienteRepo;
        this.citaRepo = citaRepo;
        this.productoRepo = productoRepo;
    }

    @GetMapping("/dashboard")
    public String verDashboard(Model model) {
        //manda de la bd, no datos estáticos
        model.addAttribute("totalPacientes", pacienteRepo.count());
        model.addAttribute("citasHoy", citaRepo.count()); // Aquí podrías filtrar por fecha
        model.addAttribute("stockCritico", productoRepo.findProductosConStockCritico().size());        return "dashboard";
    }

    @GetMapping("/inventario")
    public String verInventario() { return "inventario"; }

    @GetMapping("/reportes")
    public String verReportes() { return "reportes"; }

    @GetMapping("/configuracion")
    public String verConfiguracion() { return "configuracion"; }

    @GetMapping("/ayuda")
    public String verAyuda() { return "ayuda"; }

    @GetMapping("/") //sim
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/logout") //parche temporal
    public String simularLogout() {
        //nos faltó login profe
        return "redirect:/";
    }
}

