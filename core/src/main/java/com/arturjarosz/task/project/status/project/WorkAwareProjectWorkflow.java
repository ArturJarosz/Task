package com.arturjarosz.task.project.status.project;

import java.util.Set;

public interface WorkAwareProjectWorkflow<TProjectStatus extends ProjectStatus> {

    Set<TProjectStatus> getStatusesThatAllowCreatingWorkObjects();
}
