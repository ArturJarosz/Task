package com.arturjarosz.task.finance.application;

public interface ProjectFinanceAwareObjectService {

    void onCreate(long projectId);

    void onUpdate(long projectId);

    void onRemove(long projectId);
}
