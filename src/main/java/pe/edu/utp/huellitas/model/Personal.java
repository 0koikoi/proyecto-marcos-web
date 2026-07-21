package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "personal")
public class Personal extends pe.edu.utp.huellitas.audit.Auditable implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código único institucional. Formato: C000001 */
    @Column(name = "codigo_institucional", unique = true, nullable = false, length = 10)
    private String codigoInstitucional;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @jakarta.validation.constraints.Email(message = "Formato de correo inválido")
    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    /** Relación al rol formal (ADMINISTRADOR / RECEPCION / VETERINARIO). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Hash BCrypt almacenado en la columna 'password'. NUNCA comparar en texto
     * plano.
     */
    @Column(name = "password", nullable = false)
    private String passwordHash;

    @Column(name = "debe_cambiar_password", nullable = false)
    private Boolean debeCambiarPassword = false;

    // --- Bloqueo por intentos fallidos ---

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private OffsetDateTime bloqueadoHasta;



    // --- Helpers de nombre completo ---

    /** Nombre para mostrar en la interfaz. */
    @Transient
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public void setTelefono(String telefono) {
        if (telefono != null) {
            telefono = telefono.trim();
            if (telefono.matches("^9\\d{8}$")) {
                this.telefono = "+51 " + telefono;
            } else {
                this.telefono = telefono;
            }
        } else {
            this.telefono = null;
        }
    }

    public String getTelefono() {
        if (this.telefono != null && this.telefono.startsWith("+51 ")) {
            return this.telefono.substring(4);
        }
        return this.telefono;
    }

    public String getTelefonoCompleto() {
        return this.telefono;
    }

    // --- UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return bloqueadoHasta == null || bloqueadoHasta.isBefore(OffsetDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.activo);
    }
}

