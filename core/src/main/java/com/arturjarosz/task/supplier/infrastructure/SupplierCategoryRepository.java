package com.arturjarosz.task.supplier.infrastructure;

import com.arturjarosz.task.supplier.model.SupplierCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierCategoryRepository extends JpaRepository<SupplierCategory, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
