package com.arturjarosz.task.project.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TaskInnerDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -8940317214839882986L;

    String name;
    Long taskTypeId;
    LocalDate startDate;
    LocalDate endDate;
    String note;
    Long architectId;

}
