package com.arturjarosz.task.contractor.infrastructure;

import com.arturjarosz.task.contractor.model.ContractorCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractorCategoryRepository extends JpaRepository<ContractorCategory, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
