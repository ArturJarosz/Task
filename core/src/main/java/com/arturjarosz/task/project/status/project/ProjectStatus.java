package com.arturjarosz.task.project.status.project;

import com.arturjarosz.task.sharedkernel.status.Status;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public enum ProjectStatus implements Status<ProjectStatus> {
    TO_DO {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Arrays.asList(IN_PROGRESS, REJECTED);
        }
    },
    REJECTED {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Arrays.asList(TO_DO, IN_PROGRESS);
        }
    },
    IN_PROGRESS {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Arrays.asList(TO_DO, COMPLETED, REJECTED);
        }
    },
    DONE {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Arrays.asList(IN_PROGRESS, COMPLETED);
        }
    },
    COMPLETED {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Collections.emptyList();
        }
    };

    private final String statusName;

    ProjectStatus() {
        this.statusName = this.name();
    }

    @Override
    public String getStatusName() {
        return this.statusName;
    }
}
