package com.example.nutrivida;

import com.example.nutrivida.dto.LoginRequest;
import com.example.nutrivida.dto.LoginResponse;
import com.example.nutrivida.dto.RegistroRequest;
import com.example.nutrivida.exception.CorreoYaRegistradoException;
import com.example.nutrivida.exception.CredencialesInvalidasException;
import com.example.nutrivida.exception.RutYaRegistradoException;
import com.example.nutrivida.model.Usuario;
import com.example.nutrivida.repository.PacienteRepository;
import com.example.nutrivida.repository.UsuarioRepository;
import com.example.nutrivida.security.JwtService;
import com.example.nutrivida.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de AuthService (login y registro).
 * Los usuarios de prueba se generan con DataFaker y las contraseñas se encriptan con BCrypt real.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService("Clave-Secreta-De-Prueba-Para-JWT-NutriVida-2026", 3600000);
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, pacienteRepository, passwordEncoder, jwtService);
    }

    @Test
    @DisplayName("Login con contraseña correcta devuelve un token válido")
    void testLoginCorrecto() {
        Usuario usuario = DatosDePrueba.usuarioAleatorio("nutricionista");
        String passwordPlano = usuario.getPassword();
        usuario.setPassword(passwordEncoder.encode(passwordPlano));
        when(usuarioRepository.findByCorreoIgnoreCase(usuario.getCorreo())).thenReturn(Optional.of(usuario));

        LoginResponse respuesta = authService.login(new LoginRequest(usuario.getCorreo(), passwordPlano));

        assertTrue(jwtService.esValido(respuesta.token()));
        assertEquals("nutricionista", respuesta.rol());
        assertEquals(usuario.getNombre(), respuesta.nombre());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta lanza CredencialesInvalidasException")
    void testLoginPasswordIncorrecta() {
        Usuario usuario = DatosDePrueba.usuarioAleatorio("paciente");
        usuario.setPassword(passwordEncoder.encode("clave-correcta"));
        when(usuarioRepository.findByCorreoIgnoreCase(usuario.getCorreo())).thenReturn(Optional.of(usuario));

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(new LoginRequest(usuario.getCorreo(), "clave-incorrecta")));
    }

    @Test
    @DisplayName("Login con un correo que no existe lanza CredencialesInvalidasException")
    void testLoginCorreoInexistente() {
        String correo = DatosDePrueba.FAKER.internet().emailAddress();
        when(usuarioRepository.findByCorreoIgnoreCase(correo)).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(new LoginRequest(correo, "123456")));
    }

    @Test
    @DisplayName("Registrar guarda al paciente con la contraseña encriptada")
    void testRegistrarPaciente() {
        String nombre = DatosDePrueba.FAKER.name().fullName();
        String correo = DatosDePrueba.FAKER.internet().emailAddress();
        String rut = DatosDePrueba.rutAleatorio();
        when(usuarioRepository.existsByCorreoIgnoreCase(any())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        LoginResponse respuesta = authService.registrar(DatosDePrueba.registroAleatorio(rut, correo, "secreta123"));

        assertEquals("paciente", respuesta.rol());
        assertTrue(jwtService.esValido(respuesta.token()));
        verify(usuarioRepository).save(argThat(u ->
                !u.getPassword().equals("secreta123") && passwordEncoder.matches("secreta123", u.getPassword())));
        // También se crea el paciente en la tabla pacientes
        verify(pacienteRepository).save(argThat(p -> p.getCorreo().equals(correo.toLowerCase()) && p.getRut().equals(rut)
                && p.getTelefono() != null && p.getFechaNacimiento() != null && p.getObjetivo() != null));
    }

    @Test
    @DisplayName("Registrar un correo existente lanza CorreoYaRegistradoException")
    void testRegistrarCorreoDuplicado() {
        String correo = DatosDePrueba.FAKER.internet().emailAddress();
        when(usuarioRepository.existsByCorreoIgnoreCase(any())).thenReturn(true);

        assertThrows(CorreoYaRegistradoException.class,
                () -> authService.registrar(DatosDePrueba.registroAleatorio(DatosDePrueba.rutAleatorio(), correo, "123456")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar con contraseña corta lanza IllegalArgumentException")
    void testRegistrarPasswordCorta() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.registrar(DatosDePrueba.registroAleatorio(DatosDePrueba.rutAleatorio(), "juan@correo.cl", "123")));
    }

    @Test
    @DisplayName("Registrar con un RUT con dígito verificador incorrecto lanza IllegalArgumentException")
    void testRegistrarRutInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.registrar(DatosDePrueba.registroAleatorio("12.345.678-9", "juan@correo.cl", "123456")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar con un RUT que ya existe lanza RutYaRegistradoException")
    void testRegistrarRutDuplicado() {
        String rut = DatosDePrueba.rutAleatorio();
        when(usuarioRepository.existsByCorreoIgnoreCase(any())).thenReturn(false);
        when(pacienteRepository.existsByRut(rut)).thenReturn(true);

        assertThrows(RutYaRegistradoException.class,
                () -> authService.registrar(DatosDePrueba.registroAleatorio(rut, "juan@correo.cl", "123456")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar con un teléfono que no es celular chileno lanza IllegalArgumentException")
    void testRegistrarTelefonoInvalido() {
        RegistroRequest base = DatosDePrueba.registroAleatorio(DatosDePrueba.rutAleatorio(), "tel@correo.cl", "123456");
        RegistroRequest conTelefonoMalo = new RegistroRequest(base.nombre(), base.rut(), base.correo(), base.password(),
                "12345", base.fechaNacimiento(), base.objetivo());

        assertThrows(IllegalArgumentException.class, () -> authService.registrar(conTelefonoMalo));
    }

    @Test
    @DisplayName("Registrar sin fecha de nacimiento u objetivo lanza IllegalArgumentException")
    void testRegistrarSinFechaNiObjetivo() {
        RegistroRequest base = DatosDePrueba.registroAleatorio(DatosDePrueba.rutAleatorio(), "datos@correo.cl", "123456");

        assertThrows(IllegalArgumentException.class, () -> authService.registrar(new RegistroRequest(base.nombre(), base.rut(),
                base.correo(), base.password(), base.telefono(), null, base.objetivo())));
        assertThrows(IllegalArgumentException.class, () -> authService.registrar(new RegistroRequest(base.nombre(), base.rut(),
                base.correo(), base.password(), base.telefono(), base.fechaNacimiento(), " ")));
    }
}
