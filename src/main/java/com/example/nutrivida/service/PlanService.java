package com.example.nutrivida.service;

import com.example.nutrivida.exception.ResourceNotFoundException;
import com.example.nutrivida.model.Plan;
import com.example.nutrivida.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    public List<Plan> obtenerTodosLosPlanes() {
        return planRepository.findAll();
    }

    public Optional<Plan> obtenerPlanPorId(Long id) {
        return planRepository.findById(id);
    }

    public List<Plan> obtenerPlanesPorCategoria(String categoria) {
        return planRepository.findByCategoriaIgnoreCase(categoria);
    }

    public Plan guardarPlan(Plan plan) {
        return planRepository.save(plan);
    }

    // Actualiza un plan existente; si no existe lanza 404
    public Plan actualizarPlan(Long id, Plan datos) {
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plan no encontrado con el ID: " + id);
        }
        datos.setId(id);
        return planRepository.save(datos);
    }

    public void eliminarPlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plan no encontrado con el ID: " + id);
        }
        planRepository.deleteById(id);
    }

    public Plan aplicarDescuento(Long id, Double porcentajeDescuento) {
        Optional<Plan> planOpt = planRepository.findById(id);
        if (planOpt.isPresent()) {
            Plan plan = planOpt.get();
            Double nuevoPrecio = plan.getPrecio() * (1 - porcentajeDescuento / 100);
            plan.setPrecio(nuevoPrecio);
            return planRepository.save(plan);
        } else {
            throw new ResourceNotFoundException("Plan no encontrado con el ID: " + id);
        }
    }
}
