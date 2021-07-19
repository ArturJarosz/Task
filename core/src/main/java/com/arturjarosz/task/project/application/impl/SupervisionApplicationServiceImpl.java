package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.SupervisionApplicationService;
import com.arturjarosz.task.project.application.dto.SupervisionDto;
import com.arturjarosz.task.project.application.mapper.SupervisionDtoMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Supervision;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;

@ApplicationService
public class SupervisionApplicationServiceImpl implements SupervisionApplicationService {

    private final ProjectValidator projectValidator;
    private final ProjectRepository projectRepository;

    @Autowired
    public SupervisionApplicationServiceImpl(ProjectValidator projectValidator, ProjectRepository projectRepository) {
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
    }

    @Transactional
    @Override
    public SupervisionDto createSupervision(Long projectId, SupervisionDto supervisionDto) {
        this.projectValidator.validateProjectExistence(projectId);
        Project project = this.projectRepository.load(projectId);
        project.addSupervision(supervisionDto.isHasInvoice(), supervisionDto.getBaseNetRate(),
                supervisionDto.getHourlyNetRate(), supervisionDto.getVisitNetRate());
        project = this.projectRepository.save(project);
        Supervision supervision = project.getSupervision();
        SupervisionDto createdSupervisionDto = SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(supervision);
        return createdSupervisionDto;
    }

    @Override
    public SupervisionDto updateSupervision() {
        return null;
    }

    @Override
    public void deleteSupervision() {

    }
}
