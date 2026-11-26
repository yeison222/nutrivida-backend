package com.example.nutrivida.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Datos que envía el frontend para iniciar sesión
public record LoginRequest(
        @Schema(description = "Correo del usuario", example = "m.fernandez@nutrivida.cl") String correo,
        @Schema(description = "Contraseña", example = "123456") String password
) {}
