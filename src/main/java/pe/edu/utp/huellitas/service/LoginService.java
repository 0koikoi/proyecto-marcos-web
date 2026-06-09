package pe.edu.utp.huellitas.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.repository.PersonalRepository;

@Service
public class LoginService {

    private final PersonalRepository personalRepository;

    public LoginService(PersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }

    public Personal autenticar(String username, String password) {

        Optional<Personal> usuario =
                personalRepository.findByUsername(username);

        if(usuario.isPresent()) {

            Personal p = usuario.get();

            if(p.getPasswordHash().equals(password)
                    && p.getActivo()) {

                return p;
            }
        }

        return null;
    }
}