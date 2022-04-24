package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.finance.application.dto.TotalProjectFinancialDataDto;
import com.arturjarosz.task.finance.model.ProjectFinancialData;

public interface ProjectFinancialDataService {
    /**
     * Creates ProjectFinancialData and connects it to the Project with given projectId. If Project does not exist
     * a new exception will be thrown.
     */
    ProjectFinancialData createProjectFinancialData(Long projectId);

    /**
     * Recalculated FinancialData related to Supervision.
     */
    void recalculateSupervision(Long supervisionId, Long supervisionFinancialData);

    /**
     *
     * Recalculated ProjectFinancialData for Project with given projectId.
     */
    void recalculateProjectFinancialData(long projectId);

    /**
     * Removes ProjectFinancialData for Project with given projectId.
     */
    void removeFinancialDataForProject(Long projectId);

    /**
     * Returns total financial data of Project with given projectId. If Project does not exist, then
     * new exception will be thrown.
     */
    TotalProjectFinancialDataDto getTotalProjectFinancialData(Long projectId);
}
