package com.arturjarosz.task.finance.infrastructure;

import com.arturjarosz.task.finance.model.ProjectFinancialData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectFinancialDataRepository extends JpaRepository<ProjectFinancialData, Long> {
    ProjectFinancialData getProjectFinancialDataByProjectId(Long projectId);
}
