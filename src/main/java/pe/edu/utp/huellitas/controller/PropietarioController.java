package pe.edu.utp.huellitas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import pe.edu.utp.huellitas.model.Propietario;
import pe.edu.utp.huellitas.dto.PropietarioDTO;
import pe.edu.utp.huellitas.service.PropietarioService;

@Controller
@RequestMapping("/propietarios")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'RECEPCION', 'VETERINARIO')")
public class PropietarioController {

    private final PropietarioService propietarioService;

    public PropietarioController(PropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    @GetMapping
    public String listar(@org.springframework.web.bind.annotation.RequestParam(required = false) String buscar,
            Model model) {
        model.addAttribute("propietarios", propietarioService.buscarPropietarios(buscar));
        model.addAttribute("buscar", buscar);
        return "propietarios/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        PropietarioDTO propietario = new PropietarioDTO();

        model.addAttribute("propietario", propietario);
        model.addAttribute("titulo", "Registrar propietario");
        return "propietarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("propietario") PropietarioDTO dto,
                          BindingResult result,
                          Model model) {

        if (result.hasErrors()) {
            model.addAttribute("propietario", dto);
            model.addAttribute("titulo", dto.getId() == null ? "Registrar propietario" : "Editar propietario");
            return "propietarios/form";
        }

        Propietario propietario = null;
        if (dto.getId() != null) {
            propietario = propietarioService.obtenerPorId(dto.getId());
        } else {
            propietario = new Propietario();
        }
        
        propietario.setDni(dto.getDni());
        propietario.setNombres(dto.getNombres());
        propietario.setApellidos(dto.getApellidos());
        propietario.setTelefono(dto.getTelefono());
        propietario.setEmail(dto.getEmail());
        propietario.setDireccion(dto.getDireccion());
        
        propietarioService.guardar(propietario);
        return "redirect:/propietarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Propietario prop = propietarioService.obtenerPorId(id);
        PropietarioDTO dto = new PropietarioDTO();
        dto.setId(prop.getId());
        dto.setDni(prop.getDni());
        dto.setNombres(prop.getNombres());
        dto.setApellidos(prop.getApellidos());
        dto.setTelefono(prop.getTelefono());
        dto.setEmail(prop.getEmail());
        dto.setDireccion(prop.getDireccion());

        model.addAttribute("propietario", dto);
        model.addAttribute("titulo", "Editar propietario");
        return "propietarios/form";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String eliminar(@PathVariable Long id) {
        propietarioService.eliminar(id);
        return "redirect:/propietarios";
    }
    
    @GetMapping("/api/validar-dni")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<Boolean> validarDni(@org.springframework.web.bind.annotation.RequestParam String dni) {
        return org.springframework.http.ResponseEntity.ok(propietarioService.existeDni(dni));
    }
    
    @GetMapping("/api/validar-similar")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<Boolean> validarSimilar(@org.springframework.web.bind.annotation.RequestParam String nombres, @org.springframework.web.bind.annotation.RequestParam String telefono) {
        return org.springframework.http.ResponseEntity.ok(propietarioService.existeSimilar(nombres, telefono));
    }
}
