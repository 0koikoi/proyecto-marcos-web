package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

import pe.edu.utp.huellitas.model.Paciente;
import pe.edu.utp.huellitas.model.Propietario;
import pe.edu.utp.huellitas.service.PacienteService;
import pe.edu.utp.huellitas.service.PropietarioService;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final PropietarioService propietarioService;

    public PacienteController(PacienteService pacienteService, PropietarioService propietarioService) {
        this.pacienteService = pacienteService;
        this.propietarioService = propietarioService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("pacientes", pacienteService.listarTodos(buscar));
        model.addAttribute("buscar", buscar);
        return "pacientes/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Paciente paciente = new Paciente();
        paciente.setPropietario(new Propietario());

        cargarFormulario(model, paciente, "Registrar paciente");
        return "pacientes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Paciente paciente, BindingResult result, Model model) {
        if (result.hasErrors()) {
            if (paciente.getPropietario() == null) {
                paciente.setPropietario(new Propietario());
            }
            cargarFormulario(
                    model,
                    paciente,
                    paciente.getId() == null ? "Registrar paciente" : "Editar paciente"
            );
            return "pacientes/form";
        }

        try {
            pacienteService.guardar(paciente);
            return "redirect:/pacientes";
        } catch (IllegalArgumentException e) {
            if (paciente.getPropietario() == null) {
                paciente.setPropietario(new Propietario());
            }

            cargarFormulario(
                    model,
                    paciente,
                    paciente.getId() == null ? "Registrar paciente" : "Editar paciente"
            );

            model.addAttribute("error", e.getMessage());
            return "pacientes/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteService.obtenerPorId(id);
        cargarFormulario(model, paciente, "Editar paciente");
        return "pacientes/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return "redirect:/pacientes";
    }

    private void cargarFormulario(Model model, Paciente paciente, String titulo) {
        model.addAttribute("paciente", paciente);
        model.addAttribute("propietarios", propietarioService.listarTodos());
        model.addAttribute("titulo", titulo);
    }
}

