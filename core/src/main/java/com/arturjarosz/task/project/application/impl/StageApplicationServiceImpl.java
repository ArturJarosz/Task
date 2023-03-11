package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.contract.status.validator.ContractWorkflowValidator;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageApplicationService;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.application.mapper.StageDtoMapper;
import com.arturjarosz.task.project.domain.StageDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationService
public class StageApplicationServiceImpl implements StageApplicationService {
    public static final Logger LOG = LoggerFactory.getLogger(StageApplicationServiceImpl.class);

    private final ProjectQueryService projectQueryService;
    private final ProjectValidator projectValidator;
    private final ProjectRepository projectRepository;
    private final StageDomainService stageDomainService;
    private final StageValidator stageValidator;
    private final ContractWorkflowValidator contractWorkflowValidator;

    @Autowired
    public StageApplicationServiceImpl(ProjectQueryService projectQueryService, ProjectValidator projectValidator,
            ProjectRepository projectRepository, StageDomainService stageDomainService, StageValidator stageValidator,
            ContractWorkflowValidator contractWorkflowValidator) {
        this.projectQueryService = projectQueryService;
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.stageDomainService = stageDomainService;
        this.stageValidator = stageValidator;
        this.contractWorkflowValidator = contractWorkflowValidator;
    }

    @Transactional
    @Override
    public StageDto createStage(Long projectId, StageDto stageDto) {
        LOG.debug("Creating Stage for Project with id {}", projectId);
        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateCreateStageDto(stageDto);
        this.contractWorkflowValidator.validateContractAllowsForWorkObjectsCreation(projectId);
        Project project = maybeProject.get();
        Stage stage = this.stageDomainService.createStage(project, stageDto);
        project = this.projectRepository.save(project);
        LOG.debug("Stage for Project with id {} created.", projectId);
        return StageDtoMapper.INSTANCE.stageDtoFromStage(this.getCreatedStageWithId(project, stage));
    }

    @Transactional
    @Override
    public void removeStage(Long projectId, Long stageId) {
        LOG.debug("Removing Stage with id {} for Project with id {}.", stageId, projectId);

        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Project project = maybeProject.get();

        project.removeStage(stageId);
        this.projectRepository.save(project);
        LOG.debug("Stage with id {} for Project with id {} removed.", stageId, projectId);
    }

    @Transactional
    @Override
    public StageDto updateStage(Long projectId, Long stageId, StageDto stageDto) {
        LOG.debug("Updating Stage with id {} for Project with id {}", stageId, projectId);
        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Project project = maybeProject.get();
        this.stageValidator.validateUpdateStageDto(stageDto);
        Stage stage = this.stageDomainService.updateStage(project, stageId, stageDto);
        this.projectRepository.save(project);
        LOG.debug("Stage updated.");
        return StageDtoMapper.INSTANCE.stageDtoFromStage(stage);
    }

    @Override
    public StageDto getStage(Long projectId, Long stageId) {
        LOG.debug("Loading Stage with id {} for Project with id {}.", stageId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        LOG.debug("Stage loaded.");
        return StageDtoMapper.INSTANCE.stageDtoFromStage(stage);
    }

    @Override
    public List<StageDto> getStageListForProject(Long projectId) {
        LOG.debug("Loading list of Stages for Project with id {}.", projectId);
        this.projectValidator.validateProjectExistence(projectId);
        return this.projectQueryService.getStagesForProjectById(projectId);
    }

    @Transactional
    @Override
    public StageDto rejectStage(Long projectId, Long stageId) {
        LOG.debug("Rejecting Stage with id {}", stageId);
        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Project project = maybeProject.get();
        this.stageDomainService.rejectStage(project, stageId);
        this.projectRepository.save(project);
        return StageDtoMapper.INSTANCE.stageDtoFromStage(this.getStageById(project, stageId));
    }

    @Transactional
    @Override
    public StageDto reopenStage(Long projectId, Long stageId) {
        LOG.debug("Reopening Stage with id {}", stageId);
        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Project project = maybeProject.get();
        this.stageDomainService.reopenStage(project, stageId);
        this.projectRepository.save(project);
        return StageDtoMapper.INSTANCE.stageDtoFromStage(this.getStageById(project, stageId));
    }

    private Stage getCreatedStageWithId(Project project, Stage stage) {
        return project.getStages().stream().filter(stageOnProject -> stageOnProject.equals(stage)).findFirst()
                .orElse(null);
    }

    private Stage getStageById(Project project, Long stageId) {
        return project.getStages().stream().filter(stageOnProject -> stageOnProject.getId().equals(stageId)).findFirst()
                .orElse(null);
    }

}
