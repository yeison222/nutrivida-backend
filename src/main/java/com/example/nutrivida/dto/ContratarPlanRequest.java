package com.example.nutrivida.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// Datos que envía un paciente para contratar un plan
public record ContratarPlanRequest(
        @Schema(description = "ID del plan a contratar", example = "2") Long planId,
        @Schema(description = "Cantidad de sesiones (desde 4 se aplica 10% de descuento)", example = "4") Integer sesiones
) {}
