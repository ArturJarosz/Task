package com.arturjarosz.task.finance.domain;

import com.arturjarosz.task.finance.application.dto.ProjectFinancialDataDto;

public interface PartialFinancialDataService {
    /**
     * Method returns partial financial data for Project with projectId.
     * @param projectId
     * @return
     */
    ProjectFinancialDataDto providePartialFinancialData(long projectId);

}
