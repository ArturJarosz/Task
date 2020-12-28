package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface ProjectApplicationService {

    CreatedEntityDto createProject(ProjectCreateDto projectCreateDto);

    ProjectDto getProject(Long projectId);

    void updateProject(Long projectId, ProjectDto projectDto);

    void removeProject(Long projectId);

    void singProjectContract(Long projectId, ProjectContractDto projectContractDto);

    void finishProject(Long projectId,
                       ProjectContractDto projectContractDto);

    List<ProjectDto> getProjects();
}
