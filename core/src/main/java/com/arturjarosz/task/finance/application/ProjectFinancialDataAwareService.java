package com.arturjarosz.task.finance.application;

public interface ProjectFinancialDataAwareService {

    void triggerProjectFinancialDataRecalculation(long projectId);
}
