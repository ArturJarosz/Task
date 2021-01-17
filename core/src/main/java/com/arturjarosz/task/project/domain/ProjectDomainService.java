package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.model.Project;

import java.time.LocalDate;

public interface ProjectDomainService {

    void updateProject(Project project, ProjectDto projectDto);

    void signProjectContract(Project project, ProjectContractDto projectContractDto);

    void finishProject(Long projectId, LocalDate endDate);
}
