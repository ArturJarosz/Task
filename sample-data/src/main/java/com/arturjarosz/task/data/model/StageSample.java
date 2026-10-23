package com.arturjarosz.task.data.model;

import com.arturjarosz.task.dto.StageTypeDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StageSample {
    private String name;
    private StageTypeDto type;
    private String note;
    private Integer deadlineInDays;
    private boolean rejected;
    private InstallmentSample installment;
    private List<TaskSample> tasks = new ArrayList<>();
}
