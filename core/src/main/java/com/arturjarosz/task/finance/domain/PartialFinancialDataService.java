package com.arturjarosz.task.finance.domain;

import com.arturjarosz.task.finance.application.dto.ProjectFinancialSummaryDto;

public interface PartialFinancialDataService {
    /**
     * Method returns partial financial data for Project with projectId.
     */
    ProjectFinancialSummaryDto providePartialFinancialData(long projectId);

}
