package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;

@ApplicationService
public class ProjectFinancialDataServiceImpl implements ProjectFinancialDataService {
    private final ProjectFinancialDataRepository financialDataRepository;

    @Autowired
    public ProjectFinancialDataServiceImpl(ProjectFinancialDataRepository financialDataRepository) {
        this.financialDataRepository = financialDataRepository;
    }

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
