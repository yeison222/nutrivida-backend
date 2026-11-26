package com.example.nutrivida.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que se ejecuta en cada petición:
 * si viene el header "Authorization: Bearer <token>" y el token es válido,
 * registra al usuario como autenticado con su rol (ROLE_NUTRICIONISTA o ROLE_PACIENTE).
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.trim().toLowerCase().startsWith("bearer ")) {
            // Limpia espacios, saltos de línea y comillas que se copian por error junto al token
            String token = header.trim().substring(7).trim().replace("\"", "");
            if (jwtService.esValido(token)) {
                Claims datos = jwtService.obtenerDatos(token);
                String rol = datos.get("rol", String.class);
                UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                        datos.getSubject(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }

        filterChain.doFilter(request, response);
    }
}
