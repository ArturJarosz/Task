package com.arturjarosz.task.contractor.infrastructure;

import com.arturjarosz.task.contractor.model.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractorRepository extends JpaRepository<Contractor, Long> {
}
