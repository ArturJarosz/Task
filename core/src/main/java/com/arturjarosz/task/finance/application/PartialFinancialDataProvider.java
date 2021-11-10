package com.arturjarosz.task.finance.application;

public interface PartialFinancialDataProvider {

    void triggerProjectFinancialDataRecalculation(long projectId);
}
