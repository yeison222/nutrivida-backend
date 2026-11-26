package com.example.nutrivida.repository;

import com.example.nutrivida.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    // Spring Data genera la consulta a partir del nombre del método
    List<Plan> findByCategoriaIgnoreCase(String categoria);
}
