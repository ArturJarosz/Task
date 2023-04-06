package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.finance.application.dto.InstallmentDto;
import com.arturjarosz.task.project.model.StageType;
import com.arturjarosz.task.project.status.stage.StageStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StageDto implements Serializable {
    public static final String DEADLINE_FIELD = "deadline";
    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    public static final String STAGE_TYPE_FIELD = "stageType";
    public static final String STATUS_FIELD = "status";
    @Serial
    private static final long serialVersionUID = -8148528445387228580L;
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;
    private StageType stageType;
    private String note;
    private InstallmentDto installmentDto;
    private Integer tasksNumber;
    private StageStatus status;

}
