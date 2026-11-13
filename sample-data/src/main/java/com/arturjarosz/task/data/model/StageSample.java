package com.arturjarosz.task.data.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StageSample {
    private String name;
    // name of the Stage type
    private String type;
    private String note;
    private Integer deadlineInDays;
    // deadline the stage is created with before it is moved to deadlineInDays, shown in the history
    private Integer firstDeadlineInDays;
    private boolean rejected;
    private InstallmentSample installment;
    private List<TaskSample> tasks = new ArrayList<>();
}
