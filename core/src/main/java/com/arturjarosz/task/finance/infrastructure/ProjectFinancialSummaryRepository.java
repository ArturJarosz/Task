package com.arturjarosz.task.finance.infrastructure;

import com.arturjarosz.task.finance.model.ProjectFinancialSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectFinancialSummaryRepository extends JpaRepository<ProjectFinancialSummary, Long> {

    ProjectFinancialSummary findProjectFinancialSummaryByProjectId(long projectId);
}
