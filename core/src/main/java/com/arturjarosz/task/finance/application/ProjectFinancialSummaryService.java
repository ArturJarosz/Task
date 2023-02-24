package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.finance.application.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.model.ProjectFinancialSummary;
import com.arturjarosz.task.project.model.Project;

public interface ProjectFinancialSummaryService {
    /**
     * Creates {@link ProjectFinancialSummary} and connects it to the Project with given projectId. If {@link Project} does not exist
     * a new exception will be thrown.
     */
    ProjectFinancialSummary createProjectFinancialSummary(Long projectId);

    /**
     * Recalculated FinancialData related to Supervision.
     */
    void recalculateSupervision(Long supervisionId, Long supervisionFinancialData);

    /**
     *
     * Recalculated {@link ProjectFinancialSummary} for {@link Project} with given projectId.
     */
    void recalculateProjectFinancialSummary(long projectId);

    /**
     * Removes {@link ProjectFinancialSummary} for {@link Project} with given projectId.
     */
    void removeFinancialSummaryForProject(Long projectId);

    /**
     * Returns total financial data of {@link Project} with given projectId. If {@link Project} does not exist, then
     * new exception will be thrown.
     */
    TotalProjectFinancialSummaryDto getTotalProjectFinancialSummary(Long projectId);
}
