package com.arturjarosz.task.finance.infrastructure;

import com.arturjarosz.task.finance.model.FinancialData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialDataRepository extends JpaRepository<FinancialData, Long> {
}
