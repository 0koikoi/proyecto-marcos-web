package pe.edu.utp.huellitas.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.utp.huellitas.model.Personal;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        //si AI sesión activa
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect("/login");
            return false;
        }

        //control de permisos por roles
        Personal usuario = (Personal) session.getAttribute("usuario");
        String ruta = request.getRequestURI();

        //si es RECEPCION, no verá ni Personal, Inventario ni Proveedores
        if ("RECEPCION".equalsIgnoreCase(usuario.getCargo())) {
            if (ruta.startsWith("/personal") || ruta.startsWith("/productos") || ruta.startsWith("/proveedores")) {
                response.sendRedirect("/dashboard?error=Acceso+Denegado");
                return false;
            }
        }

        return true;//admin o veterinario
    }
}