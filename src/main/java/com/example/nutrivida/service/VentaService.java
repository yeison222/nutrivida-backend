package com.example.nutrivida.service;

import com.example.nutrivida.exception.ResourceNotFoundException;
import com.example.nutrivida.model.Paciente;
import com.example.nutrivida.model.Plan;
import com.example.nutrivida.model.Venta;
import com.example.nutrivida.repository.PacienteRepository;
import com.example.nutrivida.repository.PlanRepository;
import com.example.nutrivida.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de negocio de las ventas de planes (CRUD).
 * La venta no tiene relaciones JPA: solo guarda pacienteId y planId,
 * y el servicio revisa que ese paciente y ese plan existan.
 */
@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final PlanRepository planRepository;
    private final PacienteRepository pacienteRepository;

    // Inyección de los repositorios por constructor
    public VentaService(VentaRepository ventaRepository, PlanRepository planRepository, PacienteRepository pacienteRepository) {
        this.ventaRepository = ventaRepository;
        this.planRepository = planRepository;
        this.pacienteRepository = pacienteRepository;
    }

    // Obtener todos los registros
    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepository.findAll();
    }

    // Buscar por ID
    public Venta obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con el ID: " + id));
    }

    // Guardar / crear nuevo registro
    public Venta guardarVenta(Venta venta) {
        venta.setId(null);
        return ventaRepository.save(prepararVenta(venta));
    }

    // Actualizar registro existente
    public Venta actualizarVenta(Long id, Venta datos) {
        if (!ventaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venta no encontrada con el ID: " + id);
        }
        datos.setId(id);
        return ventaRepository.save(prepararVenta(datos));
    }

    // Eliminar por ID
    public void eliminarVenta(Long id) {
        if (!ventaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Venta no encontrada con el ID: " + id);
        }
        ventaRepository.deleteById(id);
    }

    // Un paciente con sesión contrata un plan: la venta queda a su nombre (se busca por el correo del token)
    public Venta contratarPlan(String correoPaciente, Long planId, Integer sesiones) {
        Paciente paciente = buscarPacientePorCorreo(correoPaciente);
        Venta venta = new Venta(paciente.getId(), planId, LocalDate.now(), sesiones, null, "Pagada");
        return guardarVenta(venta);
    }

    // Ventas del paciente con sesión ("Mis planes")
    public List<Venta> obtenerVentasDelPaciente(String correoPaciente) {
        return ventaRepository.findByPacienteId(buscarPacientePorCorreo(correoPaciente).getId());
    }

    private Paciente buscarPacientePorCorreo(String correo) {
        return pacienteRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new IllegalArgumentException("No hay un paciente asociado a la cuenta " + correo));
    }

    // Valida los datos y completa fecha, estado y total cuando no vienen
    private Venta prepararVenta(Venta venta) {
        if (venta.getPacienteId() == null || !pacienteRepository.existsById(venta.getPacienteId())) {
            throw new IllegalArgumentException("El paciente con ID " + venta.getPacienteId() + " no existe");
        }
        Plan plan = (venta.getPlanId() == null) ? null : planRepository.findById(venta.getPlanId()).orElse(null);
        if (plan == null) {
            throw new IllegalArgumentException("El plan con ID " + venta.getPlanId() + " no existe");
        }
        if (venta.getSesiones() == null || venta.getSesiones() <= 0) {
            throw new IllegalArgumentException("La cantidad de sesiones debe ser mayor a 0");
        }
        if (venta.getFecha() == null) {
            venta.setFecha(LocalDate.now());
        }
        if (venta.getEstado() == null || venta.getEstado().isBlank()) {
            venta.setEstado("Pendiente");
        }
        if (venta.getTotal() == null) {
            venta.setTotal(calcularTotal(plan.getPrecio(), venta.getSesiones()));
        }
        return venta;
    }

    // Precio × sesiones, con 10% de descuento desde 4 sesiones (igual que en el sitio)
    public static double calcularTotal(double precioSesion, int sesiones) {
        double total = precioSesion * sesiones;
        return sesiones >= 4 ? total * 0.9 : total;
    }
}
