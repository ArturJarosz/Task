package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
import com.arturjarosz.task.finance.application.ProjectFinancialSummaryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;

@ApplicationService
public class ProjectFinanceAwareObjectServiceImpl implements ProjectFinanceAwareObjectService {
    private final ProjectFinancialSummaryService projectFinancialSummaryService;

    @Autowired
    public ProjectFinanceAwareObjectServiceImpl(ProjectFinancialSummaryService projectFinancialSummaryService) {
        this.projectFinancialSummaryService = projectFinancialSummaryService;
    }

    @Override
    public void onCreate(long projectId) {
        this.triggerProjectFinancialSummaryRecalculation(projectId);
    }

    @Override
    public void onUpdate(long projectId) {
        this.triggerProjectFinancialSummaryRecalculation(projectId);
    }

    @Override
    public void onRemove(long projectId) {
        this.triggerProjectFinancialSummaryRecalculation(projectId);
    }

    private void triggerProjectFinancialSummaryRecalculation(long projectId){
        this.projectFinancialSummaryService.recalculateProjectFinancialSummary(projectId);
    }
}
