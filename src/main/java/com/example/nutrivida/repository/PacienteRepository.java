package com.example.nutrivida.repository;

import com.example.nutrivida.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByCorreoIgnoreCase(String correo);

    Optional<Paciente> findByCorreoIgnoreCase(String correo);

    boolean existsByRut(String rut);

    // ¿Otro paciente (distinto al id indicado) ya tiene este RUT?
    boolean existsByRutAndIdNot(String rut, Long id);
}
