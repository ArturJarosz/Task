package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.project.model.ProjectType;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@NoArgsConstructor
@Data
public class ProjectDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 8501354213746117568L;

    private Long id;
    private ClientDto client;
    private ArchitectBasicDto architect;
    private String name;
    private ProjectType projectType;
    private LocalDate signingDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;
    private String note;
    private ProjectStatus status;
    private ContractDto contractDto;

}
