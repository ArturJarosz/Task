package com.arturjarosz.task.architect.infrastructure.repository;

import com.arturjarosz.task.architect.model.Architect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchitectRepository extends JpaRepository<Architect, Long> {
}
