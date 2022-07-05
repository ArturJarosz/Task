package com.arturjarosz.task.supplier.infrastructure;

import com.arturjarosz.task.supplier.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
