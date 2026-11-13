package com.arturjarosz.task.data.model;

import com.arturjarosz.task.dto.TaskStatusDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TaskSample {
    private String name;
    // name of the Task type
    private String type;
    private String note;
    private TaskStatusDto status = TaskStatusDto.TO_DO;
    // used only when the status sets start or end date, today when not provided; stage and project dates follow them
    private Integer startedDaysAgo;
    private Integer finishedDaysAgo;
    // time logged on the task by the project's architect
    private List<TimeEntrySample> timeEntries = new ArrayList<>();
    // created and removed again after work started, shown in the history; can have no status and no time entries
    private boolean removed;
}
