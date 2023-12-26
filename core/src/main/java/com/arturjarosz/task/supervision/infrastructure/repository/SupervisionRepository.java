package com.arturjarosz.task.supervision.infrastructure.repository;

import com.arturjarosz.task.supervision.model.Supervision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupervisionRepository extends JpaRepository<Supervision, Long> {
}
