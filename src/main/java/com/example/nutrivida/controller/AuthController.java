package com.example.nutrivida.controller;

import com.example.nutrivida.dto.LoginRequest;
import com.example.nutrivida.dto.LoginResponse;
import com.example.nutrivida.dto.RegistroRequest;
import com.example.nutrivida.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Iniciar sesión",
            description = "Valida el correo y la contraseña y devuelve un **token JWT** válido por 1 hora. "
                    + "Copia el token y pégalo en el botón **Authorize** (arriba a la derecha) para usar los endpoints protegidos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto: devuelve el token",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Correo o contraseña incorrectos")))
    })
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Registrar un paciente",
            description = "Crea una cuenta con rol **paciente** (la contraseña se guarda encriptada con BCrypt), la registra también en la tabla pacientes con su RUT, teléfono, fecha de nacimiento y objetivo, y devuelve su token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente registrado",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre, RUT, correo, contraseña, teléfono, fecha de nacimiento u objetivo)",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "409", description = "El correo o el RUT ya están registrados",
                    content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/registro")
    public LoginResponse registrar(@RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }
}
