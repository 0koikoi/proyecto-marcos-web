package pe.edu.utp.huellitas.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.repository.PersonalRepository;

import java.time.OffsetDateTime;

/**
 * Listener de intentos de login fallidos.
 *
 * Regla de negocio:
 *   - Tras 3 intentos fallidos consecutivos, la cuenta se bloquea 10 minutos.
 *   - El campo bloqueadoHasta se persiste en BD.
 *   - isAccountNonLocked() en Personal.java lee este campo en tiempo real.
 *
 * Al loguear exitosamente (AuthenticationSuccessListener), los intentos se reinician.
 */
@Component
public class AuthenticationFailureListener {

    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_BLOQUEO = 10;

    private final PersonalRepository personalRepository;

    public AuthenticationFailureListener(PersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }

    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        if (username == null || username.isBlank()) {
            return;
        }

        personalRepository.findByUsername(username).ifPresent(personal -> {
            int intentos = personal.getIntentosFallidos() + 1;
            personal.setIntentosFallidos(intentos);

            if (intentos >= MAX_INTENTOS) {
                personal.setBloqueadoHasta(OffsetDateTime.now().plusMinutes(MINUTOS_BLOQUEO));
                personal.setIntentosFallidos(0); // Reinicia contador tras bloquear
            }

            personalRepository.save(personal);
        });
    }
}
