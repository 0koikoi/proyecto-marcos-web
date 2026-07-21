package pe.edu.utp.huellitas.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import pe.edu.utp.huellitas.model.Personal;

import java.util.Optional;

/**
 * Proveedor del usuario activo para Spring Data JPA Auditing.
 *
 * Spring usa este bean para rellenar automáticamente los campos
 * {@code @CreatedBy} y {@code @LastModifiedBy} en entidades que
 * extiendan {@link Auditable}.
 *
 * Habilitación: {@code @EnableJpaAuditing(auditorAwareRef = "auditorAware")}
 * en la clase de configuración principal o en {@code HuellitasApplication}.
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<Personal> {

    @Override
    public Optional<Personal> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Personal personal)) {
            return Optional.empty();
        }
        return Optional.of(personal);
    }
}
