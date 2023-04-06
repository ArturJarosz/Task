package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.project.model.TaskType;
import com.arturjarosz.task.project.status.task.TaskStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TaskDto implements Serializable {

    public static final String END_DATE_FIELD = "endDate";
    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    public static final String NOTE_FIELD = "note";
    public static final String START_DATE_FIELD = "startDate";
    public static final String STATUS_FIELD = "status";
    public static final String TASK_TYPE_FIELD = "type";
    @Serial
    private static final long serialVersionUID = 6275436633825075027L;
    private Long id;
    private String name;
    private TaskType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
    private TaskStatus status;

}
