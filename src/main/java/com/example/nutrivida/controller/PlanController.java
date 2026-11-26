package com.example.nutrivida.controller;

import com.example.nutrivida.exception.ResourceNotFoundException;
import com.example.nutrivida.model.Plan;
import com.example.nutrivida.service.PlanService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
@Tag(name = "Planes")
public class PlanController {

    @Autowired
    private PlanService planService;

    @Operation(summary = "Listar todos los planes",
            description = "Devuelve el catálogo completo de planes nutricionales. Lo usan el Inicio, el Catálogo y la Gestión de Planes del frontend.")
    @ApiResponse(responseCode = "200", description = "Lista de planes obtenida correctamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Plan.class))))
    @GetMapping
    public List<Plan> obtenerTodosLosPlanes() {
        return planService.obtenerTodosLosPlanes();
    }

    @Operation(summary = "Obtener un plan por su ID",
            description = "Busca un plan específico. Se usa en el formulario de edición del frontend.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan encontrado",
                    content = @Content(schema = @Schema(implementation = Plan.class))),
            @ApiResponse(responseCode = "404", description = "No existe un plan con ese ID",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Plan no encontrado con el ID: 99")))
    })
    @GetMapping("/{id}")
    public Plan obtenerPlanPorId(
            @Parameter(description = "ID del plan", example = "1") @PathVariable Long id) {
        return planService.obtenerPlanPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado con el ID: " + id));
    }

    @Operation(summary = "Listar planes por categoría",
            description = "Filtra los planes por categoría sin distinguir mayúsculas. Si no hay coincidencias devuelve una lista vacía.")
    @ApiResponse(responseCode = "200", description = "Planes de la categoría indicada",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Plan.class))))
    @GetMapping("/categoria/{categoria}")
    public List<Plan> obtenerPlanesPorCategoria(
            @Parameter(description = "Categoría del plan",
                    schema = @Schema(allowableValues = {"deporte", "peso", "metabolico", "vegetariano"}),
                    example = "peso")
            @PathVariable String categoria) {
        return planService.obtenerPlanesPorCategoria(categoria);
    }

    @Operation(summary = "Crear un nuevo plan",
            description = "Registra un plan en el catálogo. No se envía el `id`: lo genera la base de datos. **Requiere token de nutricionista.**")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Falta el token JWT o no es válido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El token no es de un nutricionista", content = @Content),
            @ApiResponse(responseCode = "200", description = "Plan creado correctamente (incluye el id asignado)",
                    content = @Content(schema = @Schema(implementation = Plan.class))),
            @ApiResponse(responseCode = "400", description = "El JSON enviado no tiene el formato correcto", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public Plan crearPlan(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del plan a crear", required = true,
                    content = @Content(schema = @Schema(implementation = Plan.class),
                            examples = @ExampleObject(name = "Plan Keto", value = """
                                    {
                                      "nombre": "Plan Keto",
                                      "categoria": "peso",
                                      "descripcion": "Alimentación baja en carbohidratos con control de cetosis y seguimiento semanal.",
                                      "precio": 38000,
                                      "imagen": "img/logo.png"
                                    }
                                    """)))
            @RequestBody Plan plan) {
        return planService.guardarPlan(plan);
    }

    @Operation(summary = "Actualizar un plan",
            description = "Reemplaza **todos** los datos del plan indicado. Envía el plan completo, no solo el campo que cambia. **Requiere token de nutricionista.**")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Falta el token JWT o no es válido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El token no es de un nutricionista", content = @Content),
            @ApiResponse(responseCode = "200", description = "Plan actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = Plan.class))),
            @ApiResponse(responseCode = "404", description = "No existe un plan con ese ID", content = @Content),
            @ApiResponse(responseCode = "400", description = "El JSON enviado no tiene el formato correcto", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public Plan actualizarPlan(
            @Parameter(description = "ID del plan a actualizar", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos nuevos del plan", required = true,
                    content = @Content(schema = @Schema(implementation = Plan.class),
                            examples = @ExampleObject(name = "Precio actualizado", value = """
                                    {
                                      "nombre": "Plan Nutrición Deportiva",
                                      "categoria": "deporte",
                                      "descripcion": "Optimización del rendimiento, recomposición corporal y pauta alimenticia adaptada a tu disciplina deportiva.",
                                      "precio": 48000,
                                      "imagen": "img/deporte.png"
                                    }
                                    """)))
            @RequestBody Plan plan) {
        return planService.actualizarPlan(id, plan);
    }

    @Operation(summary = "Aplicar descuento a un plan",
            description = "Rebaja el precio del plan en el porcentaje indicado. Ejemplo: 10% sobre $45.000 deja el plan en $40.500. **Requiere token de nutricionista.**")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Falta el token JWT o no es válido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El token no es de un nutricionista", content = @Content),
            @ApiResponse(responseCode = "200", description = "Descuento aplicado; devuelve el plan con el nuevo precio",
                    content = @Content(schema = @Schema(implementation = Plan.class))),
            @ApiResponse(responseCode = "404", description = "No existe un plan con ese ID",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Plan no encontrado con el ID: 99")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/descuento")
    public Plan aplicarDescuento(
            @Parameter(description = "ID del plan", example = "1") @PathVariable Long id,
            @Parameter(description = "Porcentaje de descuento (0 a 100)", example = "10") @RequestParam Double porcentaje) {
        return planService.aplicarDescuento(id, porcentaje);
    }

    @Operation(summary = "Eliminar un plan",
            description = "Borra el plan del catálogo. El frontend lo usa desde el botón Eliminar de la Gestión de Planes. **Requiere token de nutricionista.**")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Falta el token JWT o no es válido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El token no es de un nutricionista", content = @Content),
            @ApiResponse(responseCode = "200", description = "Plan eliminado correctamente", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un plan con ese ID",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Plan no encontrado con el ID: 99")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public void eliminarPlan(
            @Parameter(description = "ID del plan a eliminar", example = "4") @PathVariable Long id) {
        planService.eliminarPlan(id);
    }
}
