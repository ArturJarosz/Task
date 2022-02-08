package com.arturjarosz.task.finance.application;

/**
 * Interface for services of objects, for actions that should be triggered on create, update and remove of that object.
 */
public interface ProjectFinanceAwareObjectService {

    void onCreate(long projectId);

    void onUpdate(long projectId);

    void onRemove(long projectId);
}
