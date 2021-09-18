package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.SupervisionApplicationService;
import com.arturjarosz.task.project.application.SupervisionValidator;
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
    private final SupervisionValidator supervisionValidator;

    @Autowired
    public SupervisionApplicationServiceImpl(ProjectValidator projectValidator, ProjectRepository projectRepository,
                                             ProjectQueryService projectQueryService,
                                             SupervisionValidator supervisionValidator) {
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.projectQueryService = projectQueryService;
        this.supervisionValidator = supervisionValidator;
    }

    @Transactional
    @Override
    public SupervisionDto createSupervision(Long projectId, SupervisionDto supervisionDto) {
        this.projectValidator.validateProjectExistence(projectId);
        this.supervisionValidator.validateCreateSupervision(supervisionDto);
        Project project = this.projectRepository.load(projectId);
        project.addSupervision(supervisionDto);
        project = this.projectRepository.save(project);
        Supervision supervision = project.getSupervision();
        return SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(supervision);
    }

    @Transactional
    @Override
    public SupervisionDto updateSupervision(Long projectId, SupervisionDto supervisionDto) {
        this.projectValidator.validateProjectExistence(projectId);
        this.validateProjectHavingSupervision(projectId);
        this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        Project project = this.projectRepository.load(projectId);
        project.updateSupervision(supervisionDto);
        project = this.projectRepository.save(project);
        Supervision supervision = project.getSupervision();
        return SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(supervision);
    }

    @Transactional
    @Override
    public void deleteSupervision(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        this.validateProjectHavingSupervision(projectId);
        Project project = this.projectRepository.load(projectId);
        project.removeSupervision();
        this.projectRepository.save(project);
    }

    @Override
    public SupervisionDto getSupervision(Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        this.validateProjectHavingSupervision(projectId);
        Project project = this.projectRepository.load(projectId);
        return SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(project.getSupervision());
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

    @Override
    public SupervisionVisitDto getSupervisionVisit(Long projectId, Long supervisionVisitId) {
        this.projectValidator.validateProjectExistence(projectId);
        this.projectValidator.validateProjectHasSupervision(projectId);
        return this.projectQueryService.getProjectSupervisionVisit(
                projectId, supervisionVisitId);
    }

    private Long getIdForCreatedSupervisionVisit(Project project, SupervisionVisit supervisionVisit) {
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
