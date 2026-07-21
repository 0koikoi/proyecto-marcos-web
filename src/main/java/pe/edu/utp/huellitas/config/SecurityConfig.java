package pe.edu.utp.huellitas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import pe.edu.utp.huellitas.service.PersonalUserDetailsService;

/**
 * Configuración central de Spring Security — Huellitas v2.
 *
 * ════════════════════════════════════════════════════════════════
 * MAPA COMPLETO DE PERMISOS POR ROL
 * ════════════════════════════════════════════════════════════════
 *
 * MÓDULO               ADMINISTRADOR   RECEPCION   VETERINARIO
 * ─────────────────    ─────────────   ─────────   ───────────
 * Dashboard            ✅              ✅           ✅
 * Propietarios (ver)   ✅              ✅           ✅
 * Propietarios (CUD)   ✅              ✅           ❌
 * Propietarios (D)     ✅              ❌           ❌
 * Pacientes (ver/CE)   ✅              ✅           ✅
 * Pacientes (eliminar) ✅              ❌           ❌
 * Citas (ver)          ✅              ✅           ✅
 * Citas (crear/editar) ✅              ✅           ❌
 * Citas (cancelar)     ✅              ✅           ❌
 * Citas (eliminar)     ✅              ❌           ❌
 * Historia Clínica     ✅              ❌           ✅
 * Vacunas              ✅              ❌           ✅
 * Recetas              ✅              ❌           ✅
 * Solicitudes (crear)  ✅              ❌           ✅
 * Solicitudes (gest.)  ✅              ❌           ❌
 * Ventas               ✅              ✅           ❌
 * Inventario (ver)     ✅              ✅           ✅
 * Inventario (CUD)     ✅              ❌           ❌
 * Servicios (ver)      ✅              ✅           ✅
 * Servicios (CUD)      ✅              ❌           ❌
 * Personal             ✅              ❌           ❌
 * Proveedores          ✅              ❌           ❌
 *
 * ════════════════════════════════════════════════════════════════
 * NOTAS DE IMPLEMENTACIÓN:
 * ─ Las restricciones de URL aquí son la primera línea de defensa.
 * ─ Los @PreAuthorize en controllers/servicios son la segunda línea
 *   (defense-in-depth).
 * ─ @EnableMethodSecurity habilita @PreAuthorize, @PostAuthorize.
 * ─ Thymeleaf usa sec:authorize para ocultar elementos en la UI.
 * ════════════════════════════════════════════════════════════════
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final PersonalUserDetailsService userDetailsService;

    public SecurityConfig(PersonalUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // ─── Password encoder ─────────────────────────────────────────────────────

    /**
     * BCrypt con cost factor 12.
     * Compatible con los hashes generados por pgcrypto en el schema SQL:
     *   crypt('Huellitas2025!', gen_salt('bf', 12))
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ─── Authentication provider ──────────────────────────────────────────────

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // ─── Security filter chain ────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Autorización de rutas ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // ── Rutas públicas — sin autenticación ─────────────────────────
                .requestMatchers(
                    "/",
                    "/login",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // ── Solo ADMINISTRADOR ─────────────────────────────────────────

                // Gestión de personal y proveedores
                .requestMatchers(
                    "/personal/**",
                    "/proveedores/**"
                ).hasRole("ADMINISTRADOR")

                // Operaciones de escritura en inventario (lectura es pública para autenticados)
                .requestMatchers(
                    "/productos/guardar",
                    "/productos/eliminar/**"
                ).hasRole("ADMINISTRADOR")

                // Operaciones de escritura en servicios (lectura es pública para autenticados)
                .requestMatchers(
                    "/servicios/guardar",
                    "/servicios/eliminar/**"
                ).hasRole("ADMINISTRADOR")

                // Eliminar citas (cancelar lo puede recepción también)
                .requestMatchers(
                    "/citas/eliminar/**"
                ).hasRole("ADMINISTRADOR")

                // Eliminar propietarios
                .requestMatchers(
                    "/propietarios/eliminar/**"
                ).hasRole("ADMINISTRADOR")

                // Eliminar pacientes
                .requestMatchers(
                    "/pacientes/eliminar/**"
                ).hasRole("ADMINISTRADOR")

                // Gestión de solicitudes de material (aprobar, rechazar, entregar)
                .requestMatchers(
                    "/solicitudes/aprobar/**",
                    "/solicitudes/rechazar/**",
                    "/solicitudes/entregar/**"
                ).hasRole("ADMINISTRADOR")

                // ── ADMINISTRADOR + RECEPCION ──────────────────────────────────

                // Ventas y facturación (anulación solo admin)
                .requestMatchers(
                    "/ventas/*/anular"
                ).hasRole("ADMINISTRADOR")
                .requestMatchers(
                    "/ventas/**"
                ).hasAnyRole("ADMINISTRADOR", "RECEPCION")

                // Crear y cancelar citas
                .requestMatchers(
                    "/citas/nuevo",
                    "/citas/guardar",
                    "/citas/editar/**",
                    "/citas/cancelar/**"
                ).hasAnyRole("ADMINISTRADOR", "RECEPCION")

                // ── ADMINISTRADOR + VETERINARIO ────────────────────────────────

                // Historia clínica
                .requestMatchers(
                    "/historia/**"
                ).hasAnyRole("ADMINISTRADOR", "VETERINARIO")

                // Vacunas
                .requestMatchers(
                    "/vacunas/**"
                ).hasAnyRole("ADMINISTRADOR", "VETERINARIO")

                // Recetas médicas
                .requestMatchers(
                    "/recetas/**"
                ).hasAnyRole("ADMINISTRADOR", "VETERINARIO")

                // Solicitudes de material (crear y ver las propias)
                .requestMatchers(
                    "/solicitudes/**"
                ).hasAnyRole("ADMINISTRADOR", "VETERINARIO")

                // ── Todos los roles autenticados ───────────────────────────────
                .requestMatchers(
                    "/dashboard",
                    "/pacientes/**",
                    "/propietarios/**",
                    "/citas",
                    "/productos",
                    "/servicios",
                    "/ayuda"
                ).hasAnyRole("ADMINISTRADOR", "RECEPCION", "VETERINARIO")

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )

            // ── Form Login ────────────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")                     // GET /login → nuestro template
                .loginProcessingUrl("/login")            // POST /login → Spring Security procesa
                .defaultSuccessUrl("/dashboard", true)   // Tras login exitoso
                .failureUrl("/login?error=true")         // Tras login fallido
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )

            // ── Logout ────────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")                    // POST /logout
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)             // Destruir sesión HTTP
                .deleteCookies("JSESSIONID")             // Borrar cookie de sesión
                .clearAuthentication(true)
                .permitAll()
            )

            // ── Gestión de sesión ─────────────────────────────────────────────
            .sessionManagement(session -> session
                .maximumSessions(1)                      // Un solo dispositivo por usuario
                .expiredUrl("/login?expired=true")
            )

            // ── CSRF ──────────────────────────────────────────────────────────
            // Thymeleaf incluye el token CSRF automáticamente en todos los forms.
            // Todos los formularios de modificación (crear, editar, eliminar, cancelar)
            // deben usar method="post" — NUNCA usar GET para operaciones de escritura.
            .csrf(Customizer.withDefaults())

            // ── Provider ──────────────────────────────────────────────────────
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
