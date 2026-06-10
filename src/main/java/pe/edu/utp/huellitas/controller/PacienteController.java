package pe.edu.utp.huellitas.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import pe.edu.utp.huellitas.dto.PacienteDTO;
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
        List<PacienteDTO> pacientesDTO = pacienteService.listarTodos(buscar)
                .stream()
                .map(this::convertirADTO)
                .toList();

        model.addAttribute("pacientes", pacientesDTO);
        model.addAttribute("buscar", buscar);
        return "pacientes/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        PacienteDTO pacienteDTO = new PacienteDTO();

        cargarFormulario(model, pacienteDTO, "Registrar paciente");
        return "pacientes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("paciente") PacienteDTO pacienteDTO,
                          BindingResult result,
                          Model model) {

        if (result.hasErrors()) {
            cargarFormulario(
                    model,
                    pacienteDTO,
                    pacienteDTO.getId() == null ? "Registrar paciente" : "Editar paciente"
            );
            return "pacientes/form";
        }

        try {
            Paciente paciente = convertirAEntidad(pacienteDTO);
            pacienteService.guardar(paciente);
            return "redirect:/pacientes";
        } catch (IllegalArgumentException e) {
            cargarFormulario(
                    model,
                    pacienteDTO,
                    pacienteDTO.getId() == null ? "Registrar paciente" : "Editar paciente"
            );

            model.addAttribute("error", e.getMessage());
            return "pacientes/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Paciente paciente = pacienteService.obtenerPorId(id);
        PacienteDTO pacienteDTO = convertirADTO(paciente);

        cargarFormulario(model, pacienteDTO, "Editar paciente");
        return "pacientes/form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return "redirect:/pacientes";
    }

    private void cargarFormulario(Model model, PacienteDTO pacienteDTO, String titulo) {
        model.addAttribute("paciente", pacienteDTO);
        model.addAttribute("propietarios", propietarioService.listarTodos());
        model.addAttribute("titulo", titulo);
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