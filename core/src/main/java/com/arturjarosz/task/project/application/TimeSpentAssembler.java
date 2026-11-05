package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.ProjectDto;
import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.project.query.TimeEntryQueryService;
import com.arturjarosz.task.supervision.query.SupervisionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Sets the time spent on task and stage DTOs. Each method sums the time with one grouped query per level, not
 * one per task.
 */
@RequiredArgsConstructor
@Component
public class TimeSpentAssembler {

    private static final long MINUTES_IN_HOUR = 60L;

    private final TimeEntryQueryService timeEntryQueryService;
    private final SupervisionQueryService supervisionQueryService;

    public TaskDto setTimeSpent(TaskDto taskDto) {
        this.setTimeSpentOnTasks(List.of(taskDto));
        return taskDto;
    }

    public void setTimeSpentOnTasks(Collection<TaskDto> taskDtos) {
        var taskIds = taskDtos.stream().map(TaskDto::getId).filter(Objects::nonNull).toList();
        var minutesByTaskId = this.timeEntryQueryService.getMinutesByTaskIds(taskIds);
        taskDtos.forEach(taskDto -> taskDto.setTimeSpentMinutes(minutesByTaskId.getOrDefault(taskDto.getId(), 0L)));
    }

    public StageDto setTimeSpent(StageDto stageDto) {
        this.setTimeSpentOnStages(List.of(stageDto));
        return stageDto;
    }

    /**
     * Sets the time spent on the stages and on their tasks.
     */
    public void setTimeSpentOnStages(Collection<StageDto> stageDtos) {
        var stageIds = stageDtos.stream().map(StageDto::getId).filter(Objects::nonNull).toList();
        var minutesByStageId = this.timeEntryQueryService.getMinutesByStageIds(stageIds);
        stageDtos.forEach(
                stageDto -> stageDto.setTimeSpentMinutes(minutesByStageId.getOrDefault(stageDto.getId(), 0L)));
        var taskDtos = stageDtos.stream()
                .map(StageDto::getTasks)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .toList();
        this.setTimeSpentOnTasks(taskDtos);
    }

    /**
     * Sets the time spent on the stages of the project and on their tasks; the project's Stages tab lists them from it.
     */
    public ProjectDto setTimeSpent(ProjectDto projectDto) {
        if (projectDto.getStages() != null) {
            this.setTimeSpentOnStages(projectDto.getStages());
        }
        return projectDto;
    }

    /**
     * Returns the hours of all supervision visits of the project, payable or not, in minutes.
     */
    public long getSupervisionMinutes(long projectId) {
        return this.supervisionQueryService.getVisitHoursForProject(projectId) * MINUTES_IN_HOUR;
    }
}
