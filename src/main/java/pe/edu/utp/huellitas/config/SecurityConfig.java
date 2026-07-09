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

import pe.edu.utp.huellitas.service.PersonalUserDetailsService;

/**
 * Configuración central de Spring Security.
 *
 * Reemplaza completamente al AuthInterceptor + WebConfig anteriores.
 *
 * Reglas de acceso (en orden de precedencia):
 *   - Rutas públicas          → sin autenticación
 *   - /personal, /proveedores → solo ADMINISTRADOR
 *   - /ventas                 → ADMINISTRADOR + RECEPCION
 *   - /solicitudes            → ADMINISTRADOR (aprobar) + VETERINARIO (crear)
 *   - /historia, /vacunas, /recetas → ADMINISTRADOR + VETERINARIO
 *   - /pacientes, /propietarios, /citas, /servicios → todos los roles autenticados
 *   - /dashboard y cualquier otra ruta → autenticado
 *
 * Para restricciones más finas dentro de cada ruta (ej: veterinario solo ve
 * sus propias citas), usar @PreAuthorize en los métodos del servicio/controlador.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // Habilita @PreAuthorize, @PostAuthorize en servicios/controladores
public class SecurityConfig {

    private final PersonalUserDetailsService userDetailsService;

    public SecurityConfig(PersonalUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // ─── Password encoder ─────────────────────────────────────────────────────

    /**
     * BCrypt con cost factor 12.
     * Compatible con los hashes generados por pgcrypto en el schema SQL.
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

    // ─── Security filter chain ────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Autorización de rutas ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // Público — sin autenticación
                .requestMatchers(
                    "/",
                    "/login",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // Solo ADMINISTRADOR
                .requestMatchers(
                    "/personal/**",
                    "/proveedores/**"
                ).hasRole("ADMINISTRADOR")

                // ADMINISTRADOR + RECEPCION (ventas y facturación)
                .requestMatchers(
                    "/ventas/**"
                ).hasAnyRole("ADMINISTRADOR", "RECEPCION")

                // ADMINISTRADOR + VETERINARIO (historia clínica, recetas, vacunas)
                .requestMatchers(
                    "/historia/**",
                    "/vacunas/**",
                    "/recetas/**"
                ).hasAnyRole("ADMINISTRADOR", "VETERINARIO")

                // ADMINISTRADOR + VETERINARIO (solicitudes de material)
                // RECEPCION no puede solicitar material
                .requestMatchers(
                    "/solicitudes/**"
                ).hasAnyRole("ADMINISTRADOR", "VETERINARIO")

                // Todos los roles autenticados
                .requestMatchers(
                    "/dashboard",
                    "/pacientes/**",
                    "/propietarios/**",
                    "/citas/**",
                    "/servicios/**",
                    "/productos/**",
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
                .usernameParameter("username")           // Nombre del campo en el form
                .passwordParameter("password")           // Nombre del campo en el form
                .permitAll()
            )

            // ── Logout ────────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")                    // GET o POST /logout
                .logoutSuccessUrl("/login?logout=true")  // Tras logout exitoso
                .invalidateHttpSession(true)             // Destruir sesión HTTP
                .deleteCookies("JSESSIONID")             // Borrar cookie de sesión
                .clearAuthentication(true)
                .permitAll()
            )

            // ── Gestión de sesión ─────────────────────────────────────────────
            .sessionManagement(session -> session
                .maximumSessions(1)                      // Un solo dispositivo por usuario
                .expiredUrl("/login?expired=true")       // Si la sesión expira
            )

            // ── CSRF ──────────────────────────────────────────────────────────
            // Thymeleaf incluye el token CSRF automáticamente en todos los forms
            .csrf(Customizer.withDefaults())

            // ── Provider ──────────────────────────────────────────────────────
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
