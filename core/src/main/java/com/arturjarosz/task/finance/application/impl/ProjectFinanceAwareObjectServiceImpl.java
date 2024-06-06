package com.arturjarosz.task.finance.application.impl;

import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ProjectFinanceAwareObjectServiceImpl implements ProjectFinanceAwareObjectService {
    @NonNull
    private final ProjectFinancialDataService projectFinancialDataService;

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

    private void triggerProjectFinancialSummaryRecalculation(long projectId) {
        LOG.debug("Recalculating financial data for project with id {}", projectId);
        this.projectFinancialDataService.recalculateProjectFinancialData(projectId);
    }
}
