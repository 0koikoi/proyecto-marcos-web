package pe.edu.utp.huellitas.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.huellitas.model.Personal;
import pe.edu.utp.huellitas.model.Rol;
import pe.edu.utp.huellitas.repository.PersonalRepository;
import pe.edu.utp.huellitas.repository.RolRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión del personal de la clínica.
 * Centraliza la lógica de negocio y manejo de contraseñas.
 * Los controladores NO deben acceder a PersonalRepository ni RolRepository directamente.
 */
@Service
public class PersonalService {

    private final PersonalRepository repository;
    private final RolRepository      rolRepository;
    private final PasswordEncoder    passwordEncoder;

    public PersonalService(PersonalRepository repository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder) {
        this.repository       = repository;
        this.rolRepository    = rolRepository;
        this.passwordEncoder  = passwordEncoder;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public List<Personal> listarTodos() {
        return repository.findAll();
    }

    public Optional<Personal> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    // ── Guardar (crear o editar) ───────────────────────────────────────────────

    /**
     * Guarda o actualiza un miembro del personal.
     *
     * Reglas de negocio:
     *   - El código institucional debe tener formato C######.
     *   - Si es un registro nuevo (id == null) y se proporciona rawPassword, se hashea.
     *   - Si es una edición y rawPassword está en blanco, se conserva el hash actual.
     *
     * @param personal   Entidad con los datos del formulario
     * @param rawPassword Contraseña en texto plano (puede ser null/blank en edición)
     * @return null si OK, o un mensaje de error de negocio
     */
    @Transactional
    public String guardar(Personal personal, String rawPassword) {

        // Validación: formato del código institucional
        if (personal.getCodigoInstitucional() == null
                || !personal.getCodigoInstitucional().matches("^C\\d{6}$")) {
            return "El código institucional debe tener el formato C000000 (C seguido de 6 dígitos).";
        }

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

        // Manejo de contraseña
        if (personal.getId() == null) {
            // Creación: la contraseña es obligatoria
            if (rawPassword == null || rawPassword.isBlank()) {
                return "La contraseña es obligatoria para nuevos usuarios.";
            }
            personal.setPasswordHash(passwordEncoder.encode(rawPassword));
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

        personal.setUpdatedAt(OffsetDateTime.now());
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
        personal.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        personal.setUpdatedAt(OffsetDateTime.now());
        repository.save(personal);
        return null;
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el miembro del personal con ID: " + id);
        }
        repository.deleteById(id);
    }

    // ── Activar / Desactivar ──────────────────────────────────────────────────

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        repository.findById(id).ifPresent(p -> {
            p.setActivo(activo);
            p.setUpdatedAt(OffsetDateTime.now());
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
