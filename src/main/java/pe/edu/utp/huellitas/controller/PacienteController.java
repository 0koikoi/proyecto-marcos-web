package pe.edu.utp.huellitas.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.http.ResponseEntity;
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
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCION', 'VETERINARIO')")
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

        Paciente paciente = convertirAEntidad(pacienteDTO);
        pacienteService.guardar(paciente);
        redirectAttrs.addFlashAttribute("successMsg",
                pacienteDTO.getId() == null
                        ? "Paciente registrado correctamente."
                        : "Datos del paciente actualizados correctamente.");
        return "redirect:/pacientes";
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
        pacienteService.eliminar(id);
        redirectAttrs.addFlashAttribute("successMsg", "Paciente procesado correctamente (eliminado o desactivado).");
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
        dto.setEstado(paciente.getEstado());
        dto.setAlergias(paciente.getAlergias());
        dto.setEsterilizado(paciente.getEsterilizado());
        dto.setFechaNacimientoEstimada(paciente.getFechaNacimientoEstimada());
        dto.setPesoReferencia(paciente.getPesoReferencia());

        if (paciente.getPropietario() != null) {
            dto.setPropietarioId(paciente.getPropietario().getId());
            dto.setPropietarioNombreCompleto(paciente.getPropietario().getNombreCompleto());
            dto.setPropietarioDni(paciente.getPropietario().getDni());
        }
        return dto;
    }

    private Paciente convertirAEntidad(PacienteDTO dto) {
        Paciente paciente;
        if (dto.getId() != null) {
            paciente = pacienteService.obtenerPorId(dto.getId());
        } else {
            paciente = new Paciente();
        }
        
        paciente.setNombre(dto.getNombre());
        paciente.setEspecie(dto.getEspecie());
        paciente.setRaza(dto.getRaza());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setGenero(dto.getGenero());
        paciente.setEstado(dto.getEstado() != null ? dto.getEstado() : "ACTIVO");
        paciente.setAlergias(dto.getAlergias());
        paciente.setEsterilizado(dto.getEsterilizado() != null ? dto.getEsterilizado() : false);
        paciente.setFechaNacimientoEstimada(dto.getFechaNacimientoEstimada() != null ? dto.getFechaNacimientoEstimada() : false);

        if (dto.getPropietarioId() != null) {
            Propietario propietario = new Propietario();
            propietario.setId(dto.getPropietarioId());
            paciente.setPropietario(propietario);
        }
        return paciente;
    }
    
    @GetMapping("/api/validar-similar")
    @ResponseBody
    public ResponseEntity<Boolean> validarSimilar(@RequestParam Long propietarioId, 
                                                  @RequestParam String nombre, 
                                                  @RequestParam String especie) {
        boolean exists = pacienteService.existeSimilar(propietarioId, nombre, especie);
        return ResponseEntity.ok(exists);
    }
}
