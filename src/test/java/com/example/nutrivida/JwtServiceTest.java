package com.example.nutrivida;

import com.example.nutrivida.model.Usuario;
import com.example.nutrivida.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de JwtService (generación y validación de tokens).
 */
class JwtServiceTest {

    private static final String SECRETO = "Clave-Secreta-De-Prueba-Para-JWT-NutriVida-2026";

    private final JwtService jwtService = new JwtService(SECRETO, 3600000);

    @Test
    @DisplayName("El token generado contiene el correo y el rol del usuario")
    void testGenerarTokenContieneDatos() {
        Usuario usuario = DatosDePrueba.usuarioAleatorio("nutricionista");

        String token = jwtService.generarToken(usuario);

        assertTrue(jwtService.esValido(token));
        assertEquals(usuario.getCorreo(), jwtService.obtenerCorreo(token));
        assertEquals("nutricionista", jwtService.obtenerRol(token));
    }

    @Test
    @DisplayName("Un token modificado no es válido")
    void testTokenAlteradoNoEsValido() {
        String token = jwtService.generarToken(DatosDePrueba.usuarioAleatorio("paciente"));
        String alterado = token.substring(0, token.length() - 3) + "abc";

        assertFalse(jwtService.esValido(alterado));
    }

    @Test
    @DisplayName("Un token firmado con otra clave no es válido")
    void testTokenDeOtraClaveNoEsValido() {
        JwtService otroServicio = new JwtService("Otra-Clave-Distinta-Que-No-Es-La-Del-Servidor-2026", 3600000);
        String tokenAjeno = otroServicio.generarToken(DatosDePrueba.usuarioAleatorio("nutricionista"));

        assertFalse(jwtService.esValido(tokenAjeno));
    }

    @Test
    @DisplayName("Un token vencido no es válido")
    void testTokenVencidoNoEsValido() {
        JwtService servicioQueVence = new JwtService(SECRETO, -1000); // vence 1 segundo antes de crearse
        String tokenVencido = servicioQueVence.generarToken(DatosDePrueba.usuarioAleatorio("nutricionista"));

        assertFalse(jwtService.esValido(tokenVencido));
    }

    @Test
    @DisplayName("Un texto cualquiera no es un token válido")
    void testTextoNoEsToken() {
        assertFalse(jwtService.esValido("esto-no-es-un-token"));
    }
}
