package pe.edu.utp.huellitas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@Table(name = "personal")
public class Personal implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código único institucional. Formato: C000001 */
    @Column(unique = true, nullable = false, length = 7)
    private String codigoInstitucional;

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ. ]+$", message = "El nombre solo puede contener letras")
    @Column(nullable = false)
    private String nombreCompleto;

    /** Relación al rol formal (ADMINISTRADOR / RECEPCION / VETERINARIO). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    /** Cargo visible en la interfaz (ej: "Veterinario", "Recepcionista"). */
    @Column(nullable = false, length = 50)
    private String cargo;

    @Column(length = 100)
    private String especialidad;

    @Pattern(regexp = "^(9\\d{8})?$", message = "El teléfono debe tener 9 dígitos y comenzar con 9")
    @Column(length = 15)
    private String telefono;

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
            return this.telefono.substring(4); // Remover "+51 "
        }
        return this.telefono;
    }

    public String getTelefonoCompleto() {
        return this.telefono;
    }

    @NotBlank(message = "El correo es obligatorio")
    @jakarta.validation.constraints.Email(message = "Formato de correo inválido")
    @Column(length = 150)
    private String email;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** Hash BCrypt. NUNCA comparar en texto plano. */
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
    }

    /**
     * Spring Security usa este método para obtener la contraseña con
     * hashbycscprinjdsdjkfjsk
     */
    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    /** Spring Security usa este método para identificar al usuario. */
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
        return true;
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