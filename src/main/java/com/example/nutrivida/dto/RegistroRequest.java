package com.example.nutrivida.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

// Datos para registrar un paciente nuevo (todos obligatorios)
public record RegistroRequest(
        @Schema(description = "Nombre completo", example = "Juan Pérez") String nombre,
        @Schema(description = "RUT con dígito verificador (con o sin puntos y guion)", example = "12.345.678-5") String rut,
        @Schema(description = "Correo electrónico", example = "juan.perez@gmail.com") String correo,
        @Schema(description = "Contraseña (mínimo 6 caracteres)", example = "123456") String password,
        @Schema(description = "Celular chileno", example = "+56 9 1234 5678") String telefono,
        @Schema(description = "Fecha de nacimiento (AAAA-MM-DD)", example = "1998-05-20") LocalDate fechaNacimiento,
        @Schema(description = "Objetivo nutricional", example = "Pérdida de Peso") String objetivo
) {}
