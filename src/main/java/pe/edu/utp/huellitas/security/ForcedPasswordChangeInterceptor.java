package pe.edu.utp.huellitas.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pe.edu.utp.huellitas.model.Personal;

@Component
public class ForcedPasswordChangeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        
        // Ignorar recursos estáticos y rutas abiertas o el propio formulario de cambio
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") ||
            uri.equals("/login") || uri.equals("/logout") || uri.startsWith("/cambiar-password") ||
            uri.equals("/error") || uri.equals("/favicon.ico")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Personal) {
            Personal personal = (Personal) auth.getPrincipal();
            if (Boolean.TRUE.equals(personal.getDebeCambiarPassword())) {
                response.sendRedirect("/cambiar-password");
                return false;
            }
        }
        return true;
    }
}
