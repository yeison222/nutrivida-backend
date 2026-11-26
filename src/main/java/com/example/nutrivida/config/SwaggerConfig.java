package com.example.nutrivida.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
// Botón "Authorize" de Swagger: permite enviar el token JWT en el header Authorization
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega aquí el token que entrega POST /api/auth/login (sin la palabra Bearer)"
)
@OpenAPIDefinition(
        info = @Info(
                title = "API NutriVida - Planes Nutricionales",
                version = "1.0.0",
                description = """
                        API REST de la **Clínica NutriVida** (Temuco, Región de La Araucanía).

                        Permite administrar el catálogo de planes nutricionales que consume el frontend React:
                        listar, buscar por categoría, crear, actualizar, aplicar descuentos y eliminar planes.

                        **Categorías disponibles:** `deporte`, `peso`, `metabolico`, `vegetariano`.

                        **Autenticación JWT:** ver planes es público. Para crear, editar, aplicar descuentos o eliminar planes
                        inicia sesión en `POST /api/auth/login` con un nutricionista (ej: `m.fernandez@nutrivida.cl` / `123456`),
                        copia el `token` y pégalo en el botón **Authorize**.

                        Base de datos **MySQL** (`nutrivida`) creada y versionada con **Flyway**.
                        """,
                contact = @Contact(
                        name = "Yeison Jeldres - Duoc UC",
                        url = "https://www.duoc.cl"
                ),
                license = @License(
                        name = "Uso académico - Desarrollo Full Stack II (DSY1104)"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor local de desarrollo")
        },
        tags = {
                @Tag(name = "Autenticación", description = "Login y registro: entregan el token JWT"),
                @Tag(name = "Planes", description = "Operaciones CRUD sobre los planes nutricionales del catálogo"),
                @Tag(name = "Pacientes", description = "CRUD de pacientes (clientes) de la clínica (solo nutricionistas)"),
                @Tag(name = "Ventas", description = "CRUD de ventas de planes a pacientes (solo nutricionistas)")
        },
        // Enlace directo al frontend React que consume esta API.
        // Aparece en la parte superior de Swagger y abre la app (requiere tener corriendo "npm run dev").
        externalDocs = @ExternalDocumentation(
                description = "Abrir el frontend React de NutriVida (http://localhost:5173)",
                url = "http://localhost:5173"
        )
)
public class SwaggerConfig {
}
