package pe.edu.utp.huellitas.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utp.huellitas.dto.PacienteDTO;
import pe.edu.utp.huellitas.model.Paciente;
import pe.edu.utp.huellitas.model.Propietario;
import pe.edu.utp.huellitas.service.PacienteService;
import pe.edu.utp.huellitas.service.PropietarioService;

import java.util.List;

@Controller

@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;
    private final PropietarioService propietarioService;

    public PacienteController(PacienteService pacienteService,
            PropietarioService propietarioService) {
        this.pacienteService = pacienteService;
        this.propietarioService = propietarioService;
    }

    // lista

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        List<PacienteDTO> pacientesDTO = pacienteService.listarTodos(buscar)
                .stream()
                .map(this::convertirADTO)
                .toList();

        model.addAttribute("pacientes", pacientesDTO);
        model.addAttribute("buscar", buscar);
        model.addAttribute("activePage", "pacientes");
        return "pacientes/listar";
    }

    // form
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        cargarFormulario(model, new PacienteDTO(), "Registrar paciente");
        return "pacientes/form";
    }

    // guardar
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("paciente") PacienteDTO pacienteDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (result.hasErrors()) {
            cargarFormulario(model, pacienteDTO,
                    pacienteDTO.getId() == null ? "Registrar paciente" : "Editar paciente");
            return "pacientes/form";
        }

        try {
            Paciente paciente = convertirAEntidad(pacienteDTO);
            pacienteService.guardar(paciente);
            redirectAttrs.addFlashAttribute("successMsg",
                    pacienteDTO.getId() == null
                            ? "Paciente registrado correctamente."
                            : "Datos del paciente actualizados correctamente.");
            return "redirect:/pacientes";
        } catch (IllegalArgumentException e) {
            cargarFormulario(model, pacienteDTO,
                    pacienteDTO.getId() == null ? "Registrar paciente" : "Editar paciente");
            model.addAttribute("error", e.getMessage());
            return "pacientes/form";
        }
    }

    // editar

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            Paciente paciente = pacienteService.obtenerPorId(id);
            cargarFormulario(model, convertirADTO(paciente), "Editar paciente");
            return "pacientes/form";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "No se encontró el paciente solicitado.");
            return "redirect:/pacientes";
        }
    }

    // solo admin puede eliminar

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            pacienteService.eliminar(id);
            redirectAttrs.addFlashAttribute("successMsg", "Paciente eliminado correctamente.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg",
                    "No se pudo eliminar: el paciente tiene registros clínicos asociados. " +
                            "Solo el administrador puede gestionar esta acción.");
        }
        return "redirect:/pacientes";
    }

    // mapeo, mover después a un mapper

    private void cargarFormulario(Model model, PacienteDTO pacienteDTO, String titulo) {
        model.addAttribute("paciente", pacienteDTO);
        model.addAttribute("propietarios", propietarioService.listarTodos());
        model.addAttribute("titulo", titulo);
        model.addAttribute("activePage", "pacientes");
    }

    private PacienteDTO convertirADTO(Paciente paciente) {
        PacienteDTO dto = new PacienteDTO();
        dto.setId(paciente.getId());
        dto.setNombre(paciente.getNombre());
        dto.setEspecie(paciente.getEspecie());
        dto.setRaza(paciente.getRaza());
        dto.setFechaNacimiento(paciente.getFechaNacimiento());
        dto.setGenero(paciente.getGenero());

        if (paciente.getPropietario() != null) {
            dto.setPropietarioId(paciente.getPropietario().getId());
            dto.setPropietarioNombreCompleto(paciente.getPropietario().getNombreCompleto());
            dto.setPropietarioDni(paciente.getPropietario().getDni());
        }
        return dto;
    }

    private Paciente convertirAEntidad(PacienteDTO dto) {
        Paciente paciente = new Paciente();
        paciente.setId(dto.getId());
        paciente.setNombre(dto.getNombre());
        paciente.setEspecie(dto.getEspecie());
        paciente.setRaza(dto.getRaza());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setGenero(dto.getGenero());

        if (dto.getPropietarioId() != null) {
            Propietario propietario = new Propietario();
            propietario.setId(dto.getPropietarioId());
            paciente.setPropietario(propietario);
        }
        return paciente;
    }
}