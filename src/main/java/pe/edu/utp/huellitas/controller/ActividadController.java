package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import pe.edu.utp.huellitas.repository.CitaRepository;
import pe.edu.utp.huellitas.repository.PacienteRepository;
import pe.edu.utp.huellitas.repository.VentaRepository;

@Controller
public class ActividadController {

    private final PacienteRepository pacienteRepo;
    private final CitaRepository citaRepo;
    private final VentaRepository ventaRepo;

    public ActividadController(PacienteRepository pacienteRepo,
                               CitaRepository citaRepo,
                               VentaRepository ventaRepo) {
        this.pacienteRepo = pacienteRepo;
        this.citaRepo = citaRepo;
        this.ventaRepo = ventaRepo;
    }

    @GetMapping("/actividad")
    public String actividad(Model model) {

        model.addAttribute("pacientes",
                pacienteRepo.findTop10ByOrderByCreadoEnDesc());

        model.addAttribute("citas",
                citaRepo.findTop10ByOrderByFechaHoraDesc());

        model.addAttribute("ventas",
                ventaRepo.findTop10ByOrderByFechaDesc());

        return "actividad";
    }
}