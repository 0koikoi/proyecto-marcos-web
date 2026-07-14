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

    public WebController(PacienteRepository pacienteRepo,
            CitaRepository citaRepo,
            ProductoRepository productoRepo) {
        this.pacienteRepo = pacienteRepo;
        this.citaRepo = citaRepo;
        this.productoRepo = productoRepo;
    }

    @GetMapping("/dashboard")
    public String verDashboard(Model model) {
        model.addAttribute("totalPacientes", pacienteRepo.count());
        model.addAttribute("citasHoy", citaRepo.count());
        model.addAttribute("stockCritico", productoRepo.findProductosConStockCritico().size());
        return "dashboard";
    }
}