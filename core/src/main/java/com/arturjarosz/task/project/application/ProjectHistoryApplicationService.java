package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.HistoryEntryDto;

import java.util.List;

public interface ProjectHistoryApplicationService {

    /**
     * Returns the changes made to the project with given projectId, its contract, its stages and their tasks, removed
     * ones too, latest first. With a stageId, only the changes made to that stage and its tasks; with a taskId, only
     * those made to that task. A stage or task that is not part of the project has no changes. When the project does
     * not exist, an exception is thrown.
     */
    List<HistoryEntryDto> getProjectHistory(Long projectId, Long stageId, Long taskId);
}
