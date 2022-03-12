package com.arturjarosz.task.project.status.Contract;

import com.arturjarosz.task.sharedkernel.status.Workflow;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Set;

public class ContractWorkflow extends Workflow<ContractStatus> implements ProjectAwareContractWorkflow<ContractStatus> {
    private static final String CONTRACT_WORKFLOW = "ContractWorkflow";
    private static final Set<ContractStatus> STATUSES_THAT_ALLOW_WORKING = Sets.newHashSet(ContractStatus.ACCEPTED,
            ContractStatus.SIGNED);
    private static final Set<ContractStatus> STATUSES_THAT_ALLOW_CREATING = Sets.newHashSet(ContractStatus.OFFER,
            ContractStatus.ACCEPTED, ContractStatus.SIGNED);

    public ContractWorkflow() {
        super(CONTRACT_WORKFLOW, ContractStatus.OFFER, Arrays.asList(ContractStatus.values()));
    }

    @Override
    public Set<ContractStatus> getStatusesThatAllowWorking() {
        return STATUSES_THAT_ALLOW_WORKING;
    }

    @Override
    public Set<ContractStatus> getStatusesThatAllowCreatingProjectObjects() {
        return STATUSES_THAT_ALLOW_CREATING;
    }


}
