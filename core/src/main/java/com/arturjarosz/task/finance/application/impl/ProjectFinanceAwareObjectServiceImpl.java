package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;

@ApplicationService
public class ProjectFinanceAwareObjectServiceImpl implements ProjectFinanceAwareObjectService {
    private final ProjectFinancialDataService projectFinancialDataService;

    @Autowired
    public ProjectFinanceAwareObjectServiceImpl(ProjectFinancialDataService projectFinancialDataService) {
        this.projectFinancialDataService = projectFinancialDataService;
    }

    @Override
    public void onCreate(long projectId) {
        this.triggerProjectFinancialDataRecalculation(projectId);
    }

    @Override
    public void onUpdate(long projectId) {
        this.triggerProjectFinancialDataRecalculation(projectId);
    }

    @Override
    public void onRemove(long projectId) {
        this.triggerProjectFinancialDataRecalculation(projectId);
    }

    private void triggerProjectFinancialDataRecalculation(long projectId){
        this.projectFinancialDataService.recalculateProjectFinancialData(projectId);
    }
}
