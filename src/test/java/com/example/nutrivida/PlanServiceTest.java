package com.example.nutrivida;

import com.example.nutrivida.exception.ResourceNotFoundException;
import com.example.nutrivida.model.Plan;
import com.example.nutrivida.repository.PlanRepository;
import com.example.nutrivida.service.PlanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de PlanService.
 * El repositorio se simula con Mockito y los planes se generan con DataFaker.
 */
@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService planService;

    @Test
    @DisplayName("Listar devuelve todos los planes del repositorio")
    void testObtenerTodosLosPlanes() {
        List<Plan> planes = IntStream.rangeClosed(1, 5)
                .mapToObj(DatosDePrueba::planAleatorioConId)
                .toList();
        when(planRepository.findAll()).thenReturn(planes);

        List<Plan> resultado = planService.obtenerTodosLosPlanes();

        assertEquals(5, resultado.size());
        assertEquals(planes.get(0).getNombre(), resultado.get(0).getNombre());
    }

    @Test
    @DisplayName("Buscar por id devuelve el plan cuando existe")
    void testObtenerPlanPorId() {
        Plan plan = DatosDePrueba.planAleatorioConId(1L);
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        Optional<Plan> resultado = planService.obtenerPlanPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(plan.getNombre(), resultado.get().getNombre());
    }

    @Test
    @DisplayName("Guardar un plan llama al repositorio y devuelve el plan guardado")
    void testGuardarPlan() {
        Plan nuevo = DatosDePrueba.planAleatorio();
        when(planRepository.save(any(Plan.class))).thenAnswer(invocacion -> {
            Plan p = invocacion.getArgument(0);
            p.setId(10L);
            return p;
        });

        Plan guardado = planService.guardarPlan(nuevo);

        assertEquals(10L, guardado.getId());
        assertEquals(nuevo.getNombre(), guardado.getNombre());
        verify(planRepository).save(nuevo);
    }

    @Test
    @DisplayName("Aplicar 10% de descuento rebaja el precio correctamente")
    void testAplicarDescuento() {
        Plan plan = DatosDePrueba.planAleatorioConId(1L);
        double precioOriginal = plan.getPrecio();
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.save(plan)).thenReturn(plan);

        Plan resultado = planService.aplicarDescuento(1L, 10.0);

        assertEquals(precioOriginal * 0.9, resultado.getPrecio(), 0.01);
    }

    @Test
    @DisplayName("Aplicar descuento a un plan inexistente lanza ResourceNotFoundException")
    void testAplicarDescuentoPlanInexistente() {
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> planService.aplicarDescuento(99L, 10.0));
        verify(planRepository, never()).save(any());
    }

    @Test
    @DisplayName("Eliminar un plan existente llama a deleteById")
    void testEliminarPlanExistente() {
        when(planRepository.existsById(3L)).thenReturn(true);

        planService.eliminarPlan(3L);

        verify(planRepository).deleteById(3L);
    }

    @Test
    @DisplayName("Eliminar un plan inexistente lanza ResourceNotFoundException")
    void testEliminarPlanInexistenteLanzaExcepcion() {
        when(planRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> planService.eliminarPlan(99L));
        verify(planRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Actualizar un plan existente guarda los datos nuevos con el mismo id")
    void testActualizarPlan() {
        Plan datos = DatosDePrueba.planAleatorio();
        when(planRepository.existsById(2L)).thenReturn(true);
        when(planRepository.save(any(Plan.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Plan actualizado = planService.actualizarPlan(2L, datos);

        assertEquals(2L, actualizado.getId());
        assertEquals(datos.getNombre(), actualizado.getNombre());
    }

    @Test
    @DisplayName("Actualizar un plan inexistente lanza ResourceNotFoundException")
    void testActualizarPlanInexistente() {
        when(planRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> planService.actualizarPlan(99L, DatosDePrueba.planAleatorio()));
        verify(planRepository, never()).save(any());
    }
}
