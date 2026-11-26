package com.example.nutrivida.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "pacientes") // tabla creada por Flyway (V5)
@Schema(description = "Paciente (cliente) de la clínica NutriVida")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único (lo genera la base de datos)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(unique = true)
    @Schema(description = "RUT con dígito verificador (se guarda con puntos y guion)", example = "18.765.432-7")
    private String rut;

    @Schema(description = "Nombre completo", example = "Valentina Morales")
    private String nombre;

    @Column(unique = true)
    @Schema(description = "Correo electrónico (único)", example = "v.morales@gmail.com")
    private String correo;

    @Schema(description = "Teléfono de contacto", example = "+56 9 8765 4321")
    private String telefono;

    @Column(name = "fecha_nacimiento")
    @Schema(description = "Fecha de nacimiento (AAAA-MM-DD)", example = "1995-04-12")
    private LocalDate fechaNacimiento;

    @Schema(description = "Objetivo nutricional", example = "Nutrición Deportiva y Recomposición Corporal")
    private String objetivo;

    public Paciente() {}

    public Paciente(String rut, String nombre, String correo, String telefono, LocalDate fechaNacimiento, String objetivo) {
        this.rut = rut;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.objetivo = objetivo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
}
