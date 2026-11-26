package com.example.nutrivida;

import com.example.nutrivida.exception.ResourceNotFoundException;
import com.example.nutrivida.model.Paciente;
import com.example.nutrivida.model.Plan;
import com.example.nutrivida.model.Venta;
import com.example.nutrivida.repository.PacienteRepository;
import com.example.nutrivida.repository.PlanRepository;
import com.example.nutrivida.repository.VentaRepository;
import com.example.nutrivida.service.VentaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del CRUD de VentaService con datos generados por DataFaker.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private VentaService ventaService;

    @Test
    @DisplayName("Crear una venta sin total lo calcula con el precio del plan (10% de descuento desde 4 sesiones)")
    void testCrearVentaCalculaTotal() {
        Plan plan = DatosDePrueba.planAleatorioConId(1L);
        Venta venta = DatosDePrueba.ventaAleatoria(1L, 1L);
        venta.setSesiones(4);
        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Venta guardada = ventaService.guardarVenta(venta);

        assertEquals(plan.getPrecio() * 4 * 0.9, guardada.getTotal(), 0.01);
    }

    @Test
    @DisplayName("Crear una venta sin fecha usa la fecha de hoy")
    void testCrearVentaSinFecha() {
        Venta venta = DatosDePrueba.ventaAleatoria(1L, 1L);
        venta.setFecha(null);
        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(planRepository.findById(1L)).thenReturn(Optional.of(DatosDePrueba.planAleatorioConId(1L)));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Venta guardada = ventaService.guardarVenta(venta);

        assertEquals(java.time.LocalDate.now(), guardada.getFecha());
    }

    @Test
    @DisplayName("Crear una venta con un plan inexistente lanza IllegalArgumentException")
    void testCrearVentaPlanInexistente() {
        Venta venta = DatosDePrueba.ventaAleatoria(1L, 99L);
        when(pacienteRepository.existsById(1L)).thenReturn(true);
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ventaService.guardarVenta(venta));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear una venta con un paciente inexistente lanza IllegalArgumentException")
    void testCrearVentaPacienteInexistente() {
        Venta venta = DatosDePrueba.ventaAleatoria(99L, 1L);
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> ventaService.guardarVenta(venta));
    }

    @Test
    @DisplayName("Listar devuelve todas las ventas")
    void testListarVentas() {
        when(ventaRepository.findAll()).thenReturn(List.of(DatosDePrueba.ventaAleatoria(1L, 1L), DatosDePrueba.ventaAleatoria(2L, 3L)));

        assertEquals(2, ventaService.obtenerTodasLasVentas().size());
    }

    @Test
    @DisplayName("Actualizar una venta inexistente lanza ResourceNotFoundException")
    void testActualizarVentaInexistente() {
        when(ventaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> ventaService.actualizarVenta(99L, DatosDePrueba.ventaAleatoria(1L, 1L)));
    }

    @Test
    @DisplayName("Eliminar una venta existente llama a deleteById")
    void testEliminarVenta() {
        when(ventaRepository.existsById(2L)).thenReturn(true);

        ventaService.eliminarVenta(2L);

        verify(ventaRepository).deleteById(2L);
    }

    @Test
    @DisplayName("El cálculo del total aplica 10% de descuento solo desde 4 sesiones")
    void testCalcularTotal() {
        assertEquals(70000.0, VentaService.calcularTotal(35000, 2), 0.01);
        assertEquals(126000.0, VentaService.calcularTotal(35000, 4), 0.01);
    }

    @Test
    @DisplayName("Contratar un plan crea la venta a nombre del paciente del correo")
    void testContratarPlan() {
        Paciente paciente = DatosDePrueba.pacienteAleatorio();
        paciente.setId(7L);
        Plan plan = DatosDePrueba.planAleatorioConId(2L);
        when(pacienteRepository.findByCorreoIgnoreCase(paciente.getCorreo())).thenReturn(Optional.of(paciente));
        when(pacienteRepository.existsById(7L)).thenReturn(true);
        when(planRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Venta venta = ventaService.contratarPlan(paciente.getCorreo(), 2L, 2);

        assertEquals(7L, venta.getPacienteId());
        assertEquals(plan.getPrecio() * 2, venta.getTotal(), 0.01);
        assertEquals("Pagada", venta.getEstado());
    }

    @Test
    @DisplayName("Contratar sin un paciente asociado al correo lanza IllegalArgumentException")
    void testContratarSinPaciente() {
        when(pacienteRepository.findByCorreoIgnoreCase("nadie@correo.cl")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ventaService.contratarPlan("nadie@correo.cl", 1L, 1));
        verify(ventaRepository, never()).save(any());
    }
}
