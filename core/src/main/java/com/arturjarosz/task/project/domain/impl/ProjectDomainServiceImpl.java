package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;

@DomainService
public class ProjectDomainServiceImpl implements ProjectDomainService {

    @Override
    public void updateProject(Project project, ProjectDto projectDto) {
        project.updateProjectData(projectDto.getName(), projectDto.getNote());

    }

    @Override
    public void signProjectContract(Project project,
                                    ProjectContractDto projectContractDto) {
        project.signContract(projectContractDto.getSigningDate(), projectContractDto.getStartDate(),
                projectContractDto.getDeadline());

    }
}
