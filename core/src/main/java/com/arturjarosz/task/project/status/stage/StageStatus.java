package com.arturjarosz.task.project.status.stage;

import com.arturjarosz.task.sharedkernel.status.Status;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Enum with all possible Statuses for Stage.
 */
public enum StageStatus implements Status<StageStatus> {
    TO_DO {
        @Override
        public Collection<StageStatus> getPossibleStatusTransitions() {
            return Arrays.asList(IN_PROGRESS, REJECTED);
        }
    },

    IN_PROGRESS {
        @Override
        public Collection<StageStatus> getPossibleStatusTransitions() {
            return Arrays.asList(TO_DO, DONE, REJECTED);
        }
    },

    DONE {
        @Override
        public Collection<StageStatus> getPossibleStatusTransitions() {
            return Collections.singletonList(IN_PROGRESS);
        }
    },

    REJECTED {
        @Override
        public Collection<StageStatus> getPossibleStatusTransitions() {
            return Collections.singletonList(TO_DO);
        }
    };

    private final String statusName;

    StageStatus() {
        this.statusName = this.name();
    }

    @Override
    public String getStatusName() {
        return this.statusName;
    }
}
