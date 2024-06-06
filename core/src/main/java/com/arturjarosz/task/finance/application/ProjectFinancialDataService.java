package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.project.model.Project;

public interface ProjectFinancialDataService {
    /**
     * Creates {@link ProjectFinancialData} and connects it to the Project with given projectId. If {@link Project} does not exist
     * a new exception will be thrown.
     */
    ProjectFinancialData createProjectFinancialData(Long projectId);

    /**
     * Recalculated FinancialData related to Supervision.
     */
    void recalculateSupervision(Long supervisionId, Long supervisionFinancialData);

    /**
     * Recalculated {@link ProjectFinancialData} for {@link Project} with given projectId.
     */
    void recalculateProjectFinancialData(long projectId);

    /**
     * Removes {@link ProjectFinancialData} for {@link Project} with given projectId.
     */
    void removeFinancialDataForProject(Long projectId);

    /**
     * Returns total financial data of {@link Project} with given projectId. If {@link Project} does not exist, then
     * new exception will be thrown.
     */
    TotalProjectFinancialSummaryDto getTotalProjectFinancialData(Long projectId);
}
