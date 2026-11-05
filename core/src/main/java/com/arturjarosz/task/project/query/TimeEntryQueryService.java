package com.arturjarosz.task.project.query;

import com.arturjarosz.task.project.model.TimeEntry;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TimeEntryQueryService {

    /**
     * Returns time entries of the task, latest work date first and, for the same work date, latest created first.
     */
    List<TimeEntry> getTimeEntriesForTask(long taskId);

    boolean timeEntryExistsInTask(long taskId, long timeEntryId);

    boolean architectHasTimeEntries(long architectId);

    /**
     * Returns minutes logged on each of the tasks; tasks without entries get 0.
     */
    Map<Long, Long> getMinutesByTaskIds(Collection<Long> taskIds);

    /**
     * Returns minutes logged on the tasks of each of the stages; stages without entries get 0.
     */
    Map<Long, Long> getMinutesByStageIds(Collection<Long> stageIds);

    /**
     * Returns minutes logged on the tasks of the project, without its supervision visits.
     */
    long getMinutesForProject(long projectId);
}
