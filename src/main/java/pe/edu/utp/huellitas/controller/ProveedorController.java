package pe.edu.utp.huellitas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.model.Proveedor;
import pe.edu.utp.huellitas.service.ProveedorService;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    // MOSTRAR VISTA + LISTAR
  @GetMapping
public String listar(Model model) {
    model.addAttribute("listaProveedores", proveedorService.listar());
    model.addAttribute("nuevoProveedor", new Proveedor());
    model.addAttribute("activePage", "proveedores"); // Esto activa el color azul en el menú
    return "proveedores";
}
    

    // GUARDAR
   @PostMapping("/guardar")
    public String guardar(
        @ModelAttribute Proveedor proveedor,
        HttpSession session) {

    Personal usuario =
            (Personal) session.getAttribute("usuario");

    if(usuario == null ||
       !usuario.getCargo().equals("ADMINISTRADOR")) {

        return "redirect:/dashboard?error=AccesoDenegado";
    }

    proveedorService.guardar(proveedor);

    return "redirect:/proveedores";
}

    // ELIMINAR
    @GetMapping("/eliminar/{id}")
    public String eliminar(
        @PathVariable Long id,
        HttpSession session) {

    Personal usuario =
            (Personal) session.getAttribute("usuario");

    if(usuario == null ||
       !usuario.getCargo().equals("ADMINISTRADOR")) {

        return "redirect:/dashboard?error=AccesoDenegado";
    }

    proveedorService.eliminar(id);

    return "redirect:/proveedores";
}

    //editar

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {

    Proveedor proveedor =
            proveedorService.buscarPorId(id);

    model.addAttribute("nuevoProveedor", proveedor);

    model.addAttribute(
            "listaProveedores",
            proveedorService.listar());

    model.addAttribute(
            "activePage",
            "proveedores");

    return "proveedores";
    }
    
}

