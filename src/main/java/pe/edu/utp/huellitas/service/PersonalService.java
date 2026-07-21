package pe.edu.utp.huellitas.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.model.Rol;
import pe.edu.utp.huellitas.repository.PersonalRepository;
import pe.edu.utp.huellitas.repository.RolRepository;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión del personal de la clínica.
 * Centraliza la lógica de negocio y manejo de contraseñas.
 * Los controladores NO deben acceder a PersonalRepository ni RolRepository
 * directamente.
 */
@Service
public class PersonalService {

    private final PersonalRepository repository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public PersonalService(PersonalRepository repository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<Personal> listarTodos() {
        return repository.findAll();
    }

    public List<Personal> listarVeterinarios() {
        return repository.findByRolNombre("VETERINARIO");
    }

    public Optional<Personal> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Optional<Personal> obtenerPorUsername(String username) {
        return repository.findByUsername(username);
    }

    // ── Guardar (crear o editar) ───────────────────────────────────────────────

    /**
     * Guarda o actualiza un miembro del personal.
     *
     * Reglas de negocio:
     * - El código institucional debe tener formato C######.
     * - Si es un registro nuevo (id == null) y se proporciona rawPassword, se
     * hashea.
     * - Si es una edición y rawPassword está en blanco, se conserva el hash actual.
     *
     * @param personal    Entidad con los datos del formulario
     * @param rawPassword Contraseña en texto plano (puede ser null/blank en
     *                    edición)
     * @return null si OK, o un mensaje de error de negocio
     */
    @Transactional
    public String guardar(Personal personal, String rawPassword) {

        // Validación: el rol debe existir
        if (personal.getRol() == null || personal.getRol().getId() == null) {
            return "Debe seleccionar un rol válido.";
        }
        Rol rol = rolRepository.findById(personal.getRol().getId())
                .orElse(null);
        if (rol == null) {
            return "El rol seleccionado no existe.";
        }
        personal.setRol(rol);

        // Manejo de código institucional (solo creación) y contraseña
        if (personal.getId() == null) {
            personal.setCodigoInstitucional(generarCodigoIdentificador(rol));
            
            // Creación: la contraseña es obligatoria
            if (rawPassword == null || rawPassword.isBlank()) {
                return "La contraseña es obligatoria para nuevos usuarios.";
            }
            personal.setPasswordHash(passwordEncoder.encode(rawPassword));
            personal.setDebeCambiarPassword(true);
        } else {
            // Edición: solo actualiza hash si se proporcionó una nueva contraseña
            if (rawPassword != null && !rawPassword.isBlank()) {
                personal.setPasswordHash(passwordEncoder.encode(rawPassword));
            } else {
                // Conservar el hash actual desde BD
                repository.findById(personal.getId())
                        .ifPresent(existing -> personal.setPasswordHash(existing.getPasswordHash()));
            }
        }

        personal.setActualizadoEn(OffsetDateTime.now());
        repository.save(personal);
        return null; // null = sin errores
    }

    // ── Cambio de contraseña ──────────────────────────────────────────────────

    @Transactional
    public String cambiarPassword(Long id, String passwordActual, String nuevaPassword) {
        Personal personal = repository.findById(id).orElse(null);
        if (personal == null) {
            return "Usuario no encontrado.";
        }
        if (!passwordEncoder.matches(passwordActual, personal.getPasswordHash())) {
            return "La contraseña actual es incorrecta.";
        }
        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            return "La nueva contraseña debe tener al menos 8 caracteres.";
        }
        if (!nuevaPassword.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$")) {
            return "La contraseña debe tener al menos una mayúscula, un número y un símbolo especial.";
        }
        personal.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        personal.setDebeCambiarPassword(false);
        personal.setActualizadoEn(OffsetDateTime.now());
        repository.save(personal);
        return null;
    }

    public String generarPasswordTemporal() {
        String mayusculas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numeros = "0123456789";
        SecureRandom random = new SecureRandom();
        
        StringBuilder temp = new StringBuilder("temp-");
        // 3 letras aleatorias
        for (int i = 0; i < 3; i++) {
            temp.append(mayusculas.charAt(random.nextInt(mayusculas.length())));
        }
        // 1 número
        temp.append(numeros.charAt(random.nextInt(numeros.length())));
        return temp.toString();
    }

    private String generarCodigoIdentificador(Rol rol) {
        String prefijo = "C";
        if (rol != null && rol.getNombre() != null) {
            if (rol.getNombre().equalsIgnoreCase("ADMINISTRADOR")) prefijo = "A";
            else if (rol.getNombre().equalsIgnoreCase("RECEPCION")) prefijo = "R";
            else if (rol.getNombre().equalsIgnoreCase("VETERINARIO")) prefijo = "V";
        }
        
        SecureRandom random = new SecureRandom();
        int numeroAleatorio = random.nextInt(1000000); // 0 a 999999
        return String.format("%s%06d", prefijo, numeroAleatorio);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    /**
     * Desactiva al personal (activo = false).
     * Soft delete para no romper FK en el historial.
     */
    @Transactional
    public void desactivar(Long id) {
        repository.findById(id).ifPresent(p -> {
            p.setActivo(false);
            p.setActualizadoEn(OffsetDateTime.now());
            repository.save(p);
        });
    }

    /**
     * Elimina físicamente al usuario de la base de datos.
     * REGLA DE NEGOCIO: NUNCA hacer DELETE físico.
     */
    @Transactional
    public void eliminar(Long id) {
        throw new pe.edu.utp.huellitas.exception.NegocioException("La política de la clínica no permite eliminar físicamente al personal para preservar el historial de auditoría y registros médicos. Utilice la opción 'Desactivar'.");
    }

    // ── Activar / Desactivar ──────────────────────────────────────────────────

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        repository.findById(id).ifPresent(p -> {
            p.setActivo(activo);
            p.setActualizadoEn(OffsetDateTime.now());
            repository.save(p);
        });
    }

    // ── Catálogos auxiliares ──────────────────────────────────────────────────

    /**
     * Lista todos los roles disponibles.
     * Usado en formularios de creación/edición de personal.
     * El controller NO debe inyectar RolRepository directamente.
     */
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }
}
