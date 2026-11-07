package com.arturjarosz.task.systemparameter.infrastructure.repository;

import com.arturjarosz.task.systemparameter.model.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemParameterRepository extends JpaRepository<SystemParameter, Long> {

    boolean existsByName(String name);

    Optional<SystemParameter> findByName(String name);
}
