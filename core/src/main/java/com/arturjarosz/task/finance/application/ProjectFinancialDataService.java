package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.finance.model.ProjectFinancialData;

public interface ProjectFinancialDataService {

    ProjectFinancialData joinFinancialDataWithProject(Long projectId);

    void removeProjectFinancialData(Long projectId);
}
