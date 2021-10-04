package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinancialDataApplicationService;
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;

@ApplicationService
public class ProjectFinancialDataApplicationServiceImpl implements ProjectFinancialDataApplicationService {

    private final ProjectFinancialDataRepository projectFinancialDataRepository;
    private final ProjectValidator projectValidator;

    public ProjectFinancialDataApplicationServiceImpl(ProjectFinancialDataRepository projectFinancialDataRepository,
                                                      ProjectValidator projectValidator) {
        this.projectFinancialDataRepository = projectFinancialDataRepository;
        this.projectValidator = projectValidator;
    }

    @Override
    public ProjectFinancialData createProjectFinancialData(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        ProjectFinancialData projectFinancialData = new ProjectFinancialData(projectId);
        projectFinancialData.initiateProjectFinancialData();
        projectFinancialData = this.projectFinancialDataRepository.save(projectFinancialData);
        return projectFinancialData;
    }

    public void recalculateProjectFinancialData(Long projectId){
        // get project installments
        // get project costs
        // get project commissions
        // get supervisions
        // recalculate
    }
}
