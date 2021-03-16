package com.arturjarosz.task.status.domain;

import com.arturjarosz.task.sharedkernel.status.Status;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public enum TaskStatus implements Status<TaskStatus> {
    TO_DO {
        @Override
        public Collection<TaskStatus> getPossibleStatusTransitions() {
            return Arrays.asList(IN_PROGRESS, REJECTED);
        }
    },
    IN_PROGRESS {
        @Override
        public Collection<TaskStatus> getPossibleStatusTransitions() {
            return Arrays.asList(TO_DO, DONE, REJECTED);
        }
    },
    DONE {
        @Override
        public Collection<TaskStatus> getPossibleStatusTransitions() {
            return Collections.singletonList(IN_PROGRESS);
        }
    },
    REJECTED {
        @Override
        public Collection<TaskStatus> getPossibleStatusTransitions() {
            return Collections.singletonList(TO_DO);
        }
    };

    private final String statusName;

    TaskStatus() {
        this.statusName = this.name();
    }

    @Override
    public String getStatusName() {
        return this.statusName;
    }
}
