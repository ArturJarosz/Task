package com.arturjarosz.task.project.status.contract;

import com.arturjarosz.task.sharedkernel.status.Status;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public enum ContractStatus implements Status<ContractStatus> {
    OFFER {
        @Override
        public Collection<ContractStatus> getPossibleStatusTransitions() {
            return Arrays.asList(REJECTED, ACCEPTED);
        }
    },
    REJECTED {
        @Override
        public Collection<ContractStatus> getPossibleStatusTransitions() {
            return Collections.singletonList(OFFER);
        }
    },
    ACCEPTED {
        @Override
        public Collection<ContractStatus> getPossibleStatusTransitions() {
            return Arrays.asList(REJECTED, SIGNED);
        }
    },
    SIGNED {
        @Override
        public Collection<ContractStatus> getPossibleStatusTransitions() {
            return Arrays.asList(TERMINATED, COMPLETED);
        }
    },
    TERMINATED {
        @Override
        public Collection<ContractStatus> getPossibleStatusTransitions() {
            return Collections.singletonList(TERMINATED);
        }
    },
    COMPLETED {
        @Override
        public Collection<ContractStatus> getPossibleStatusTransitions() {
            return Collections.emptyList();
        }
    };
    private final String statusName;

    ContractStatus() {
        this.statusName = this.name();
    }

    @Override
    public String getStatusName() {
        return this.statusName;
    }
}
