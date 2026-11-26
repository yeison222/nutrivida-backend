package com.example.nutrivida.controller;

import com.example.nutrivida.dto.ContratarPlanRequest;
import com.example.nutrivida.model.Venta;
import com.example.nutrivida.service.VentaService;
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

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Falta el token JWT o no es válido", content = @Content),
        @ApiResponse(responseCode = "403", description = "El token no tiene el rol necesario (paciente en /contratar y /mis-ventas, nutricionista en el resto)", content = @Content)
})
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @Operation(summary = "Listar ventas", description = "Devuelve todas las ventas de planes.")
    @ApiResponse(responseCode = "200", description = "Lista de ventas",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Venta.class))))
    @GetMapping
    public List<Venta> obtenerTodasLasVentas() {
        return ventaService.obtenerTodasLasVentas();
    }

    @Operation(summary = "Contratar un plan (paciente)",
            description = "El **paciente con sesión** contrata un plan. La venta queda a su nombre (se identifica por el correo de su token), "
                    + "con la fecha de hoy y el total calculado. **Requiere token de paciente.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan contratado",
                    content = @Content(schema = @Schema(implementation = Venta.class))),
            @ApiResponse(responseCode = "400", description = "El plan no existe o las sesiones no son válidas", content = @Content)
    })
    @PostMapping("/contratar")
    public Venta contratarPlan(@RequestBody ContratarPlanRequest request, Principal usuario) {
        return ventaService.contratarPlan(usuario.getName(), request.planId(), request.sesiones());
    }

    @Operation(summary = "Mis planes contratados (paciente)",
            description = "Devuelve las ventas del **paciente con sesión**. **Requiere token de paciente.**")
    @ApiResponse(responseCode = "200", description = "Ventas del paciente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Venta.class))))
    @GetMapping("/mis-ventas")
    public List<Venta> obtenerMisVentas(Principal usuario) {
        return ventaService.obtenerVentasDelPaciente(usuario.getName());
    }

    @Operation(summary = "Buscar una venta por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada",
                    content = @Content(schema = @Schema(implementation = Venta.class))),
            @ApiResponse(responseCode = "404", description = "No existe una venta con ese ID",
                    content = @Content(mediaType = "text/plain",
                            examples = @ExampleObject(value = "Venta no encontrada con el ID: 99")))
    })
    @GetMapping("/{id}")
    public Venta obtenerVentaPorId(@Parameter(description = "ID de la venta", example = "1") @PathVariable Long id) {
        return ventaService.obtenerVentaPorId(id);
    }

    @Operation(summary = "Registrar una venta",
            description = "Vende un plan a un paciente. Si no se envía `total`, se calcula con el precio del plan × sesiones "
                    + "(10% de descuento desde 4 sesiones). Si no se envía `fecha`, se usa la fecha de hoy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta registrada",
                    content = @Content(schema = @Schema(implementation = Venta.class))),
            @ApiResponse(responseCode = "400", description = "El paciente o el plan no existen, o las sesiones no son válidas", content = @Content)
    })
    @PostMapping
    public Venta crearVenta(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = Venta.class),
                            examples = @ExampleObject(name = "Venta de 4 sesiones", value = """
                                    {
                                      "pacienteId": 1,
                                      "planId": 2,
                                      "sesiones": 4,
                                      "estado": "Pagada"
                                    }
                                    """)))
            @RequestBody Venta venta) {
        return ventaService.guardarVenta(venta);
    }

    @Operation(summary = "Actualizar una venta", description = "Reemplaza todos los datos de la venta indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta actualizada",
                    content = @Content(schema = @Schema(implementation = Venta.class))),
            @ApiResponse(responseCode = "404", description = "No existe una venta con ese ID", content = @Content)
    })
    @PutMapping("/{id}")
    public Venta actualizarVenta(@Parameter(description = "ID de la venta", example = "1") @PathVariable Long id,
                                 @RequestBody Venta venta) {
        return ventaService.actualizarVenta(id, venta);
    }

    @Operation(summary = "Eliminar una venta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe una venta con ese ID", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void eliminarVenta(@Parameter(description = "ID de la venta", example = "3") @PathVariable Long id) {
        ventaService.eliminarVenta(id);
    }
}
