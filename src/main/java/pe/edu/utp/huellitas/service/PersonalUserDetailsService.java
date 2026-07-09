package pe.edu.utp.huellitas.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pe.edu.utp.huellitas.repository.PersonalRepository;

/**
 * Servicio que Spring Security llama internamente para cargar
 * el usuario desde la base de datos durante la autenticación.
 *
 * Spring Security invoca loadUserByUsername() en el flujo de login.
 * NO es necesario invocarlo manualmente desde ningún controlador.
 */
@Service
public class PersonalUserDetailsService implements UserDetailsService {

    private final PersonalRepository personalRepository;

    public PersonalUserDetailsService(PersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return personalRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe ningún usuario con el username: " + username));
    }
}
