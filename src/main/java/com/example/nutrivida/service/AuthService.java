package com.example.nutrivida.service;

import com.example.nutrivida.dto.LoginRequest;
import com.example.nutrivida.dto.LoginResponse;
import com.example.nutrivida.dto.RegistroRequest;
import com.example.nutrivida.exception.CorreoYaRegistradoException;
import com.example.nutrivida.exception.CredencialesInvalidasException;
import com.example.nutrivida.exception.RutYaRegistradoException;
import com.example.nutrivida.model.Paciente;
import com.example.nutrivida.model.Usuario;
import com.example.nutrivida.repository.PacienteRepository;
import com.example.nutrivida.repository.UsuarioRepository;
import com.example.nutrivida.security.JwtService;
import com.example.nutrivida.util.RutUtil;
import com.example.nutrivida.util.TelefonoUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PacienteRepository pacienteRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Verifica correo y contraseña; si son correctos devuelve un token JWT
    public LoginResponse login(LoginRequest request) {
        if (request.correo() == null || request.password() == null) {
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos");
        }
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(request.correo().trim())
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos");
        }

        return new LoginResponse(jwtService.generarToken(usuario), usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }

    // Registra un paciente nuevo (la contraseña se guarda encriptada, y sus datos quedan en la tabla pacientes) y devuelve su token
    public LoginResponse registrar(RegistroRequest request) {
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (!RutUtil.esValido(request.rut())) {
            throw new IllegalArgumentException("El RUT no es válido (revisa el dígito verificador)");
        }
        if (request.correo() == null || !request.correo().contains("@")) {
            throw new IllegalArgumentException("El correo no es válido");
        }
        if (request.password() == null || request.password().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
        if (!TelefonoUtil.esValido(request.telefono())) {
            throw new IllegalArgumentException("El teléfono debe ser un celular chileno (ej: +56 9 1234 5678)");
        }
        if (request.fechaNacimiento() == null
                || request.fechaNacimiento().isAfter(LocalDate.now())
                || request.fechaNacimiento().isBefore(LocalDate.now().minusYears(120))) {
            throw new IllegalArgumentException("La fecha de nacimiento no es válida");
        }
        if (request.objetivo() == null || request.objetivo().isBlank()) {
            throw new IllegalArgumentException("El objetivo nutricional es obligatorio");
        }
        String correo = request.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new CorreoYaRegistradoException("El correo " + correo + " ya está registrado");
        }
        String rut = RutUtil.formatear(request.rut()); // se guarda siempre como 12.345.678-5
        if (pacienteRepository.existsByRut(rut)) {
            throw new RutYaRegistradoException("El RUT " + rut + " ya está registrado");
        }

        Usuario nuevo = new Usuario(request.nombre().trim(), correo, passwordEncoder.encode(request.password()), "paciente");
        Usuario guardado = usuarioRepository.save(nuevo);

        // También se registra como paciente de la clínica (tabla pacientes), si aún no existe
        if (!pacienteRepository.existsByCorreoIgnoreCase(correo)) {
            pacienteRepository.save(new Paciente(rut, guardado.getNombre(), correo,
                    TelefonoUtil.formatear(request.telefono()), request.fechaNacimiento(), request.objetivo().trim()));
        }

        return new LoginResponse(jwtService.generarToken(guardado), guardado.getNombre(), guardado.getCorreo(), guardado.getRol());
    }
}
