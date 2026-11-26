package com.example.nutrivida.service;

import com.example.nutrivida.exception.CorreoYaRegistradoException;
import com.example.nutrivida.exception.ResourceNotFoundException;
import com.example.nutrivida.exception.RutYaRegistradoException;
import com.example.nutrivida.model.Paciente;
import com.example.nutrivida.repository.PacienteRepository;
import com.example.nutrivida.util.RutUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lógica de negocio de los pacientes (CRUD).
 */
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    // Inyección del repositorio por constructor
    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    // Obtener todos los registros
    public List<Paciente> obtenerTodosLosPacientes() {
        return pacienteRepository.findAll();
    }

    // Buscar por ID
    public Paciente obtenerPacientePorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con el ID: " + id));
    }

    // Guardar / crear nuevo registro
    public Paciente guardarPaciente(Paciente paciente) {
        validar(paciente);
        if (pacienteRepository.existsByCorreoIgnoreCase(paciente.getCorreo())) {
            throw new CorreoYaRegistradoException("Ya existe un paciente con el correo " + paciente.getCorreo());
        }
        if (pacienteRepository.existsByRut(paciente.getRut())) {
            throw new RutYaRegistradoException("Ya existe un paciente con el RUT " + paciente.getRut());
        }
        paciente.setId(null);
        return pacienteRepository.save(paciente);
    }

    // Actualizar registro existente
    public Paciente actualizarPaciente(Long id, Paciente datos) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente no encontrado con el ID: " + id);
        }
        validar(datos);
        if (pacienteRepository.existsByRutAndIdNot(datos.getRut(), id)) {
            throw new RutYaRegistradoException("Ya existe otro paciente con el RUT " + datos.getRut());
        }
        datos.setId(id);
        return pacienteRepository.save(datos);
    }

    // Eliminar por ID
    public void eliminarPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente no encontrado con el ID: " + id);
        }
        pacienteRepository.deleteById(id);
    }

    private void validar(Paciente paciente) {
        if (paciente.getNombre() == null || paciente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del paciente es obligatorio");
        }
        if (paciente.getCorreo() == null || !paciente.getCorreo().contains("@")) {
            throw new IllegalArgumentException("El correo del paciente no es válido");
        }
        if (!RutUtil.esValido(paciente.getRut())) {
            throw new IllegalArgumentException("El RUT del paciente no es válido (revisa el dígito verificador)");
        }
        paciente.setRut(RutUtil.formatear(paciente.getRut())); // formato 12.345.678-5
    }
}
