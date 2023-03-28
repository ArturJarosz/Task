package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.project.model.ProjectType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ProjectCreateDto implements Serializable {
    private static final long serialVersionUID = -7596108006634813082L;

    private Long id;
    private String name;
    private Long architectId;
    private Long clientId;
    private ProjectType projectType;
    private Double offerValue;
    private LocalDate deadline;

}
