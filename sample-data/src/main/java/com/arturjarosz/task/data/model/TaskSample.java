package com.arturjarosz.task.data.model;

import com.arturjarosz.task.dto.TaskStatusDto;
import lombok.Getter;
import lombok.Setter;

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
}
