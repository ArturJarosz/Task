package com.arturjarosz.task.companycost.infrastructure;

import com.arturjarosz.task.companycost.model.CompanyCost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyCostRepository extends JpaRepository<CompanyCost, Long> {
}
