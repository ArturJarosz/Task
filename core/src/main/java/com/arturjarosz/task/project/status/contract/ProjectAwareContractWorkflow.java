package com.arturjarosz.task.project.status.contract;

import java.util.Set;

/**
 * Interface that has to be implemented by Contract Workflow, that is going to be used in Project. This interface is
 * responsible for providing statuses, that can be used for progressing with work or creating new Project objects,
 * such as Tasks or Stages.
 */
public interface ProjectAwareContractWorkflow<TContractStatus extends ContractStatus> {
    /**
     * Returns all Contract statuses, that allows for any work on Project, such as changing Project statuses.
     */
    Set<TContractStatus> getStatusesThatAllowWorking();

    /**
     * Returs all Contract statuses, that allows for creating new Project objects, such as Tasks or Stages.
     */
    Set<TContractStatus> getStatusesThatAllowCreatingProjectObjects();
}
