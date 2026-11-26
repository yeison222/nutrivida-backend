package com.example.nutrivida.controller;

import com.example.nutrivida.model.Paciente;
import com.example.nutrivida.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Falta el token JWT o no es válido", content = @Content),
        @ApiResponse(responseCode = "403", description = "El token no es de un nutricionista", content = @Content)
})
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @Operation(summary = "Listar pacientes", description = "Devuelve todos los pacientes registrados.")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Paciente.class))))
    @GetMapping
    public List<Paciente> obtenerTodosLosPacientes() {
        return pacienteService.obtenerTodosLosPacientes();
    }

    @Operation(summary = "Buscar un paciente por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado",
                    content = @Content(schema = @Schema(implementation = Paciente.class))),
            @ApiResponse(responseCode = "404", description = "No existe un paciente con ese ID",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Paciente no encontrado con el ID: 99")))
    })
    @GetMapping("/{id}")
    public Paciente obtenerPacientePorId(@Parameter(description = "ID del paciente", example = "1") @PathVariable Long id) {
        return pacienteService.obtenerPacientePorId(id);
    }

    @Operation(summary = "Crear un paciente", description = "No se envía el `id`: lo genera la base de datos. El correo no puede repetirse.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente creado",
                    content = @Content(schema = @Schema(implementation = Paciente.class))),
            @ApiResponse(responseCode = "400", description = "Nombre vacío, correo inválido o RUT inválido", content = @Content),
            @ApiResponse(responseCode = "409", description = "El correo o el RUT ya están registrados", content = @Content)
    })
    @PostMapping
    public Paciente crearPaciente(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = Paciente.class),
                            examples = @ExampleObject(name = "Paciente nuevo", value = """
                                    {
                                      "rut": "20.123.456-5",
                                      "nombre": "Martín Sepúlveda",
                                      "correo": "m.sepulveda@gmail.com",
                                      "telefono": "+56 9 5555 4444",
                                      "fechaNacimiento": "1999-06-15",
                                      "objetivo": "Pérdida de Peso"
                                    }
                                    """)))
            @RequestBody Paciente paciente) {
        return pacienteService.guardarPaciente(paciente);
    }

    @Operation(summary = "Actualizar un paciente", description = "Reemplaza todos los datos del paciente indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente actualizado",
                    content = @Content(schema = @Schema(implementation = Paciente.class))),
            @ApiResponse(responseCode = "404", description = "No existe un paciente con ese ID", content = @Content)
    })
    @PutMapping("/{id}")
    public Paciente actualizarPaciente(@Parameter(description = "ID del paciente", example = "1") @PathVariable Long id,
                                       @RequestBody Paciente paciente) {
        return pacienteService.actualizarPaciente(id, paciente);
    }

    @Operation(summary = "Eliminar un paciente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un paciente con ese ID", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void eliminarPaciente(@Parameter(description = "ID del paciente", example = "3") @PathVariable Long id) {
        pacienteService.eliminarPaciente(id);
    }
}
