package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationService
public class ProjectFinancialDataServiceImpl implements ProjectFinancialDataService {
    @NonNull
    private final ProjectFinancialDataRepository financialDataRepository;

    @Override
    public ProjectFinancialData joinFinancialDataWithProject(Long projectId) {
        ProjectFinancialData financialData = new ProjectFinancialData(projectId);
        this.financialDataRepository.save(financialData);
        return financialData;
    }

    @Override
    public void removeProjectFinancialData(Long projectId) {
        ProjectFinancialData projectFinancialData = this.financialDataRepository.getProjectFinancialDataByProjectId(
                projectId);
        this.financialDataRepository.delete(projectFinancialData);
    }
}
