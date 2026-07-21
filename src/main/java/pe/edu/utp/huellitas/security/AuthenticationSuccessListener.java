package pe.edu.utp.huellitas.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.repository.PersonalRepository;

/**
 * Listener de login exitoso.
 * Reinicia el contador de intentos fallidos al autenticarse correctamente.
 */
@Component
public class AuthenticationSuccessListener {

    private final PersonalRepository personalRepository;

    public AuthenticationSuccessListener(PersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (!(principal instanceof Personal)) {
            return;
        }
        Personal personal = (Personal) principal;

        // Solo actualiza si había intentos acumulados
        if (personal.getIntentosFallidos() > 0 || personal.getBloqueadoHasta() != null) {
            personal.setIntentosFallidos(0);
            personal.setBloqueadoHasta(null);
            personalRepository.save(personal);
        }
    }
}
