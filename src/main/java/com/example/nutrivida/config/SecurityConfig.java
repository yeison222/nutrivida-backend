package com.example.nutrivida.config;

import com.example.nutrivida.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Reglas de seguridad de la API:
 * - Públicos: login/registro, ver planes y Swagger.
 * - Crear, editar, descontar y eliminar planes: solo con token JWT de un nutricionista.
 * - CRUD de pacientes y de ventas: solo con token JWT de un nutricionista.
 * - Contratar un plan y ver "mis planes": solo con token JWT de un paciente.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API REST sin sesión: no se usa CSRF ni sesiones de servidor, solo el token
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // usa la configuración de CorsConfig
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(antMatcher("/error")).permitAll()
                        .requestMatchers(antMatcher("/api/auth/**")).permitAll()
                        .requestMatchers(antMatcher("/swagger"), antMatcher("/swagger-ui/**"),
                                antMatcher("/swagger-ui.html"), antMatcher("/api-docs/**")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/planes/**")).permitAll()
                        .requestMatchers(antMatcher("/api/planes/**")).hasRole("NUTRICIONISTA")
                        .requestMatchers(antMatcher("/api/pacientes/**")).hasRole("NUTRICIONISTA")
                        // El paciente contrata y ve solo sus planes (estas reglas van antes de la regla general de ventas)
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/ventas/contratar")).hasRole("PACIENTE")
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/ventas/mis-ventas")).hasRole("PACIENTE")
                        .requestMatchers(antMatcher("/api/ventas/**")).hasRole("NUTRICIONISTA")
                        .anyRequest().authenticated())
                .exceptionHandling(errores -> errores
                        // Sin token o con token inválido -> 401
                        .authenticationEntryPoint((request, response, ex) ->
                                escribirError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Token requerido o inválido. Inicia sesión en /api/auth/login"))
                        // Con token, pero sin el rol necesario -> 403
                        .accessDeniedHandler((request, response, ex) ->
                                escribirError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "No tienes permisos para esta acción (requiere rol nutricionista)")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Encriptador de contraseñas (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void escribirError(HttpServletResponse response, int estado, String mensaje) throws IOException {
        response.setStatus(estado);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(mensaje);
    }
}
