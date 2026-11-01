package com.arturjarosz.task.finance.infrastructure;

import com.arturjarosz.task.finance.model.CostCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostCategoryRepository extends JpaRepository<CostCategory, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
