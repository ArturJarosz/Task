package com.arturjarosz.task.contract.status;

import com.arturjarosz.task.sharedkernel.status.WorkAwareStatusWorkflow;
import com.arturjarosz.task.sharedkernel.status.Workflow;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class ContractStatusWorkflow extends Workflow<ContractStatus> implements WorkAwareStatusWorkflow<ContractStatus> {
    private static final String CONTRACT_WORKFLOW = "ContractWorkflow";
    private static final Set<ContractStatus> STATUSES_THAT_ALLOW_WORKING = Set.of(ContractStatus.ACCEPTED,
            ContractStatus.SIGNED);
    private static final Set<ContractStatus> STATUSES_THAT_ALLOW_CREATING = Set.of(ContractStatus.OFFER,
            ContractStatus.ACCEPTED, ContractStatus.SIGNED);

    public ContractStatusWorkflow() {
        super(CONTRACT_WORKFLOW, ContractStatus.OFFER, Arrays.asList(ContractStatus.values()));
    }

    @Override
    public Set<ContractStatus> getStatusesThatAllowWorking() {
        return STATUSES_THAT_ALLOW_WORKING;
    }

    @Override
    public Set<ContractStatus> getStatusesThatAllowCreatingWorkObjects() {
        return STATUSES_THAT_ALLOW_CREATING;
    }


}
