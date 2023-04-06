package com.arturjarosz.task.project.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ProjectContractDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 7397583738097957909L;

    private Long id;
    private LocalDate signingDate;
    private LocalDate deadline;
    //TODO: TA-34 add project value when signing contract

}
