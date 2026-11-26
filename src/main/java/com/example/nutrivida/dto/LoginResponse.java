package com.example.nutrivida.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Respuesta del login: el token JWT y los datos básicos del usuario
public record LoginResponse(
        @Schema(description = "Token JWT para enviar en el header Authorization: Bearer <token>") String token,
        @Schema(example = "Dra. María Paz Fernández") String nombre,
        @Schema(example = "m.fernandez@nutrivida.cl") String correo,
        @Schema(example = "nutricionista") String rol
) {}
