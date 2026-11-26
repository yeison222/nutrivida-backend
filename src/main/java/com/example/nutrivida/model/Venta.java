package com.example.nutrivida.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Venta de un plan nutricional a un paciente.
 * Es una entidad independiente: guarda el id del paciente y el id del plan como números,
 * sin relaciones JPA (@ManyToOne), como pide el alcance del proyecto.
 */
@Entity
@Table(name = "ventas") // tabla creada por Flyway (V5)
@Schema(description = "Venta de un plan nutricional a un paciente")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único (lo genera la base de datos)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "paciente_id")
    @Schema(description = "ID del paciente que compra", example = "1")
    private Long pacienteId;

    @Column(name = "plan_id")
    @Schema(description = "ID del plan comprado", example = "1")
    private Long planId;

    @Schema(description = "Fecha de la venta (si no se envía, se usa la fecha de hoy)", example = "2026-11-19")
    private LocalDate fecha;

    @Schema(description = "Cantidad de sesiones compradas", example = "4")
    private Integer sesiones;

    @Schema(description = "Total en CLP (si no se envía, se calcula: precio del plan × sesiones, con 10% de descuento desde 4 sesiones)", example = "162000")
    private Double total;

    @Schema(description = "Estado de la venta", example = "Pagada", allowableValues = {"Pagada", "Pendiente", "Anulada"})
    private String estado;

    public Venta() {}

    public Venta(Long pacienteId, Long planId, LocalDate fecha, Integer sesiones, Double total, String estado) {
        this.pacienteId = pacienteId;
        this.planId = planId;
        this.fecha = fecha;
        this.sesiones = sesiones;
        this.total = total;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Integer getSesiones() { return sesiones; }
    public void setSesiones(Integer sesiones) { this.sesiones = sesiones; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
