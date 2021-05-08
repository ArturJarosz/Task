package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.model.Project;

import java.time.LocalDate;

public interface ProjectDomainService {
    Project createProject(ProjectCreateDto projectCreateDto);

    Project updateProject(Project project, ProjectDto projectDto);

    Project signProjectContract(Project project, ProjectContractDto projectContractDto);

    Project finishProject(Project project, LocalDate endDate);

    Project rejectProject(Project project);

    Project makeNewOffer(Project project);
}
