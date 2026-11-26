package com.example.nutrivida.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "planes") // tabla creada por Flyway (V1__crear_tabla_planes.sql)
@Schema(description = "Plan nutricional del catálogo de NutriVida")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // el id lo genera la columna IDENTITY de la tabla
    @Schema(description = "Identificador único (lo genera la base de datos)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del plan", example = "Plan Nutrición Deportiva")
    private String nombre;

    @Schema(description = "Categoría del plan", example = "deporte", allowableValues = {"deporte", "peso", "metabolico", "vegetariano"})
    private String categoria;

    @Column(length = 500)
    @Schema(description = "Descripción del servicio", example = "Optimización del rendimiento y recomposición corporal.")
    private String descripcion;

    @Schema(description = "Precio por sesión en pesos chilenos (CLP)", example = "45000")
    private double precio;

    @Schema(description = "Ruta de la imagen en el frontend", example = "img/deporte.png")
    private String imagen;

    public Plan() {}

    public Plan(String nombre, String categoria, String descripcion, double precio, String imagen) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagen = imagen;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}
