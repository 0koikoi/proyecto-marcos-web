package pe.edu.utp.huellitas.controller;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import pe.edu.utp.huellitas.model.EstadoCita;
import pe.edu.utp.huellitas.repository.CitaRepository;
import pe.edu.utp.huellitas.repository.DetalleVentaRepository;
import pe.edu.utp.huellitas.repository.PacienteRepository;
import pe.edu.utp.huellitas.repository.ProductoRepository;
import pe.edu.utp.huellitas.repository.VentaRepository;

@Controller
public class WebController {

    private final PacienteRepository pacienteRepo;
    private final CitaRepository citaRepo;
    private final ProductoRepository productoRepo;
    private final VentaRepository ventaRepo;
    private final DetalleVentaRepository detalleVentaRepo;

    public WebController(PacienteRepository pacienteRepo,
            CitaRepository citaRepo,
            ProductoRepository productoRepo,
            VentaRepository ventaRepo,
            DetalleVentaRepository detalleVentaRepo) {

        this.pacienteRepo = pacienteRepo;
        this.citaRepo = citaRepo;
        this.productoRepo = productoRepo;
        this.ventaRepo = ventaRepo;
        this.detalleVentaRepo = detalleVentaRepo;
    }

    @GetMapping("/dashboard")
    public String verDashboard(Model model) {

        model.addAttribute("totalPacientes", pacienteRepo.count());
        model.addAttribute("citasHoy", citaRepo.count());
        model.addAttribute("stockCritico", productoRepo.findStockCritico().size());

        model.addAttribute("ventasMes", ventaRepo.totalVentasMes());
        model.addAttribute("ultimosPacientes", pacienteRepo.findTop10ByOrderByCreatedAtDesc());
        model.addAttribute("ultimasCitas", citaRepo.findTop10ByOrderByFechaHoraDesc());
        model.addAttribute("ultimasVentas", ventaRepo.findTop10ByOrderByFechaEmisionDesc());
        model.addAttribute("solicitudesPendientes", 0);
        model.addAttribute("productosMasVendidos", detalleVentaRepo.productosMasVendidos());

        model.addAttribute(
                "proximasCitas",
                citaRepo.findTop5ByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(
                        EstadoCita.PENDIENTE,
                        OffsetDateTime.now()));

        return "dashboard";
    }

}