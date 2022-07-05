package com.arturjarosz.task.systemparameter.infrastructure.repository;

import com.arturjarosz.task.systemparameter.model.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemParameterRepository extends JpaRepository<SystemParameter, Long> {
}
