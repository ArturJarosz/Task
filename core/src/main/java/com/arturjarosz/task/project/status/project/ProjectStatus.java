package com.arturjarosz.task.project.status.project;

import com.arturjarosz.task.sharedkernel.status.Status;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public enum ProjectStatus implements Status<ProjectStatus> {
    OFFER {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Arrays.asList(REJECTED, TO_DO);
        }
    },
    REJECTED {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Arrays.asList(OFFER, TO_DO, IN_PROGRESS);
        }
    },
    TO_DO {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Arrays.asList(IN_PROGRESS, REJECTED);

        }
    },

    IN_PROGRESS {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Arrays.asList(TO_DO, COMPLETED, REJECTED);
        }
    },

    COMPLETED {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Arrays.asList(IN_PROGRESS, DONE);
        }
    },

    DONE {
        @Override
        public Collection<ProjectStatus> getPossibleStatusTransitions() {
            return Collections.singletonList(IN_PROGRESS);
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
