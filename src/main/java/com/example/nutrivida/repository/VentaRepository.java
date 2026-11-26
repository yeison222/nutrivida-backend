package com.example.nutrivida.repository;

import com.example.nutrivida.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Ventas de un paciente (para "Mis planes")
    List<Venta> findByPacienteId(Long pacienteId);
}
