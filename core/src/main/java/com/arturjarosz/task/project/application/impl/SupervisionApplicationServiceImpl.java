package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.SupervisionApplicationService;
import com.arturjarosz.task.project.application.dto.SupervisionDto;
import com.arturjarosz.task.project.application.dto.SupervisionVisitDto;
import com.arturjarosz.task.project.application.mapper.SupervisionDtoMapper;
import com.arturjarosz.task.project.application.mapper.SupervisionVisitDtoMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Supervision;
import com.arturjarosz.task.project.model.SupervisionVisit;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;

@ApplicationService
public class SupervisionApplicationServiceImpl implements SupervisionApplicationService {

    private final ProjectValidator projectValidator;
    private final ProjectRepository projectRepository;
    private final ProjectQueryService projectQueryService;

    @Autowired
    public SupervisionApplicationServiceImpl(ProjectValidator projectValidator, ProjectRepository projectRepository,
                                             ProjectQueryService projectQueryService) {
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.projectQueryService = projectQueryService;
    }

    @Transactional
    @Override
    public SupervisionDto createSupervision(Long projectId, SupervisionDto supervisionDto) {
        this.projectValidator.validateProjectExistence(projectId);
        Project project = this.projectRepository.load(projectId);
        //TODO: TA-185 add data validation
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

    @Transactional
    @Override
    public SupervisionVisitDto createSupervisionVisit(Long projectId, SupervisionVisitDto supervisionVisitDto) {
        this.projectValidator.validateProjectExistence(projectId);
        this.validateProjectHavingSupervision(projectId);
        Project project = this.projectRepository.load(projectId);
        //TODO: TA-185 Add data validation
        SupervisionVisit supervisionVisit = new SupervisionVisit(supervisionVisitDto.getDateOfVisit(),
                supervisionVisitDto.getHoursCount(), supervisionVisitDto.isPayable());
        project.addSupervisionVisit(supervisionVisit);
        project = this.projectRepository.save(project);
        SupervisionVisitDto createdSupervisionVisitDto = SupervisionVisitDtoMapper.INSTANCE
                .supervisionVisitDtoFromSupervisionVision(supervisionVisit);
        createdSupervisionVisitDto.setId(this.getIdForCreatedSupervisionVisit(project, supervisionVisit));
        return createdSupervisionVisitDto;
    }

    private long getIdForCreatedSupervisionVisit(Project project, SupervisionVisit supervisionVisit) {
        return project.getSupervision().getSupervisionVisits().stream()
                .filter(visit -> visit.equals(supervisionVisit))
                .map(AbstractEntity::getId)
                .findFirst().orElse(null);
    }

    private void validateProjectHavingSupervision(Long projectId) {
        BaseValidator.assertNotNull(this.projectQueryService.projectHasSupervision(projectId),
                BaseValidator.createMessageCode(
                        ExceptionCodes.NOT_EXISTS, ProjectExceptionCodes.PROJECT, ProjectExceptionCodes.SUPERVISION),
                projectId);
    }
}
