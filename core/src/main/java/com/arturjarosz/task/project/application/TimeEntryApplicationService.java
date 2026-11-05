package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.TimeEntryDto;

import java.util.List;

public interface TimeEntryApplicationService {

    /**
     * Returns time entries of the Task with given taskId, latest work date first. If Project, Stage or Task does not
     * exist, an exception is thrown.
     */
    List<TimeEntryDto> getTimeEntries(Long projectId, Long stageId, Long taskId);

    /**
     * Logs time spent on the Task with given taskId. If Project, Stage or Task does not exist, or the data does not
     * meet the criteria for a time entry, an exception is thrown and nothing is saved.
     */
    TimeEntryDto createTimeEntry(Long projectId, Long stageId, Long taskId, TimeEntryDto timeEntryDto);

    /**
     * Updates the time entry with given timeEntryId of the Task with given taskId. If any of them does not exist, or
     * the data does not meet the criteria for a time entry, an exception is thrown and the entry is not changed.
     */
    TimeEntryDto updateTimeEntry(Long projectId, Long stageId, Long taskId, Long timeEntryId,
            TimeEntryDto timeEntryDto);

    /**
     * Deletes the time entry with given timeEntryId of the Task with given taskId. If any of them does not exist, an
     * exception is thrown.
     */
    void deleteTimeEntry(Long projectId, Long stageId, Long taskId, Long timeEntryId);
}
