package com.example.nutrivida;

import com.example.nutrivida.exception.CorreoYaRegistradoException;
import com.example.nutrivida.exception.ResourceNotFoundException;
import com.example.nutrivida.exception.RutYaRegistradoException;
import com.example.nutrivida.model.Paciente;
import com.example.nutrivida.repository.PacienteRepository;
import com.example.nutrivida.service.PacienteService;
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
 * Pruebas unitarias del CRUD de PacienteService con pacientes generados por DataFaker.
 */
@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    @Test
    @DisplayName("Listar devuelve todos los pacientes")
    void testListarPacientes() {
        List<Paciente> pacientes = List.of(DatosDePrueba.pacienteAleatorio(), DatosDePrueba.pacienteAleatorio(), DatosDePrueba.pacienteAleatorio());
        when(pacienteRepository.findAll()).thenReturn(pacientes);

        assertEquals(3, pacienteService.obtenerTodosLosPacientes().size());
    }

    @Test
    @DisplayName("Buscar por id devuelve el paciente")
    void testBuscarPorId() {
        Paciente paciente = DatosDePrueba.pacienteAleatorio();
        paciente.setId(1L);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        Paciente resultado = pacienteService.obtenerPacientePorId(1L);

        assertEquals(paciente.getCorreo(), resultado.getCorreo());
    }

    @Test
    @DisplayName("Buscar un paciente inexistente lanza ResourceNotFoundException")
    void testBuscarInexistente() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pacienteService.obtenerPacientePorId(99L));
    }

    @Test
    @DisplayName("Crear un paciente lo guarda en el repositorio")
    void testCrearPaciente() {
        Paciente nuevo = DatosDePrueba.pacienteAleatorio();
        when(pacienteRepository.existsByCorreoIgnoreCase(nuevo.getCorreo())).thenReturn(false);
        when(pacienteRepository.save(nuevo)).thenReturn(nuevo);

        Paciente guardado = pacienteService.guardarPaciente(nuevo);

        assertEquals(nuevo.getNombre(), guardado.getNombre());
        verify(pacienteRepository).save(nuevo);
    }

    @Test
    @DisplayName("Crear un paciente con un correo repetido lanza CorreoYaRegistradoException")
    void testCrearPacienteCorreoRepetido() {
        Paciente nuevo = DatosDePrueba.pacienteAleatorio();
        when(pacienteRepository.existsByCorreoIgnoreCase(nuevo.getCorreo())).thenReturn(true);

        assertThrows(CorreoYaRegistradoException.class, () -> pacienteService.guardarPaciente(nuevo));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar un paciente existente mantiene su id")
    void testActualizarPaciente() {
        Paciente datos = DatosDePrueba.pacienteAleatorio();
        when(pacienteRepository.existsById(2L)).thenReturn(true);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Paciente actualizado = pacienteService.actualizarPaciente(2L, datos);

        assertEquals(2L, actualizado.getId());
        assertEquals(datos.getNombre(), actualizado.getNombre());
    }

    @Test
    @DisplayName("Actualizar un paciente inexistente lanza ResourceNotFoundException")
    void testActualizarInexistente() {
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> pacienteService.actualizarPaciente(99L, DatosDePrueba.pacienteAleatorio()));
    }

    @Test
    @DisplayName("Eliminar un paciente existente llama a deleteById")
    void testEliminarPaciente() {
        when(pacienteRepository.existsById(3L)).thenReturn(true);

        pacienteService.eliminarPaciente(3L);

        verify(pacienteRepository).deleteById(3L);
    }

    @Test
    @DisplayName("Crear un paciente con un RUT repetido lanza RutYaRegistradoException")
    void testCrearPacienteRutRepetido() {
        Paciente nuevo = DatosDePrueba.pacienteAleatorio();
        when(pacienteRepository.existsByCorreoIgnoreCase(nuevo.getCorreo())).thenReturn(false);
        when(pacienteRepository.existsByRut(nuevo.getRut())).thenReturn(true);

        assertThrows(RutYaRegistradoException.class, () -> pacienteService.guardarPaciente(nuevo));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear un paciente con un RUT inválido lanza IllegalArgumentException")
    void testCrearPacienteRutInvalido() {
        Paciente nuevo = DatosDePrueba.pacienteAleatorio();
        nuevo.setRut("11.111.111-2"); // el dígito verificador correcto es 1

        assertThrows(IllegalArgumentException.class, () -> pacienteService.guardarPaciente(nuevo));
    }
}
