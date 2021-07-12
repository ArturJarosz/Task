package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.finance.model.ProjectFinancialData;

public interface ProjectFinancialDataApplicationService {
    /**
     * Creates ProjectFinancialData and connects it to the Project with given projectId. If Project does not exist
     * a new excepotion will be thrown.
     *
     * @param projectId
     * @return
     */
    ProjectFinancialData createProjectFinancialData(Long projectId);
}
