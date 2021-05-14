package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageApplicationService;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.application.mapper.StageDtoMapper;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageWorkflow;
import com.arturjarosz.task.project.status.stage.StageWorkflowService;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationService
public class StageApplicationServiceImpl implements StageApplicationService {
    public static final Logger LOG = LoggerFactory.getLogger(StageApplicationServiceImpl.class);

    private final ProjectQueryService projectQueryService;
    private final ProjectValidator projectValidator;
    private final ProjectRepository projectRepository;
    private final StageValidator stageValidator;
    private final StageWorkflow stageWorkflow;
    private final StageWorkflowService stageWorkflowService;

    @Autowired
    public StageApplicationServiceImpl(ProjectQueryService projectQueryService,
                                       ProjectValidator projectValidator, ProjectRepository projectRepository,
                                       StageValidator stageValidator,
                                       StageWorkflow stageWorkflow,
                                       StageWorkflowService stageWorkflowService) {
        this.projectQueryService = projectQueryService;
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.stageValidator = stageValidator;
        this.stageWorkflow = stageWorkflow;
        this.stageWorkflowService = stageWorkflowService;
    }

    @Transactional
    @Override
    public StageDto createStage(Long projectId,
                                StageDto stageDto) {
        LOG.debug("Creating Stage for Project with id {}", projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateCreateStageDto(stageDto);
        Stage stage = StageDtoMapper.INSTANCE.stageCreateDtoToStage(stageDto, this.stageWorkflow);
        Project project = this.projectRepository.load(projectId);
        project.addStage(stage);
        this.stageWorkflowService.changeStageStatusOnProject(project, stage.getId(), StageStatus.TO_DO);
        project = this.projectRepository.save(project);
        LOG.debug("Stage for Project with id {} created.", projectId);
        return StageDtoMapper.INSTANCE.stageDtoFromStage(stage);
    }

    @Transactional
    @Override
    public void removeStage(Long projectId, Long stageId) {
        LOG.debug("Removing Stage with id {} for Project with id {}.", stageId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Project project = this.projectRepository.load(projectId);
        project.removeStage(stageId);
        LOG.debug("Stage with id {} for Project with id {} removed.", stageId, projectId);
    }

    @Transactional
    @Override
    public StageDto updateStage(Long projectId, Long stageId, StageDto stageDto) {
        LOG.debug("Updating Stage with id {} for Project with id {}", stageId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Project project = this.projectRepository.load(projectId);
        this.stageValidator.validateUpdateStageDto(stageDto);
        Stage stage = project.updateStage(stageId, stageDto.getName(), stageDto.getNote(), stageDto.getStageType(),
                stageDto.getDeadline());
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
    public List<StageDto> getStageBasicList(
            Long projectId) {
        LOG.debug("Loading list of Stages for Project with id {}.", projectId);
        this.projectValidator.validateProjectExistence(projectId);
        Project project = this.projectRepository.load(projectId);
        return project.getStages()
                .stream()
                .map(StageDtoMapper.INSTANCE::stageToStageBasicDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void rejectStage(Long projectId, Long stageId) {
        LOG.debug("Rejecting Stage with id {}", stageId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Project project = this.projectRepository.load(projectId);
        this.stageWorkflowService.changeStageStatusOnProject(project, stageId, StageStatus.REJECTED);
        this.projectRepository.save(project);
    }

    @Transactional
    @Override
    public void reopenStage(Long projectId, Long stageId) {
        LOG.debug("Reopening Stage with id {}", stageId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Project project = this.projectRepository.load(projectId);
        StageStatus newStatus = this
                .stageHasOnlyTasksInToDoStatus(stageId) ? StageStatus.TO_DO : StageStatus.IN_PROGRESS;
        this.stageWorkflowService.changeStageStatusOnProject(project, stageId, newStatus);
        this.projectRepository.save(project);
    }

    private boolean stageHasOnlyTasksInToDoStatus(Long stageId) {
        Stage stage = this.projectQueryService.getStageById(stageId);
        List<Task> allTasks = new ArrayList<>(stage.getTasks());
        allTasks.removeIf(task -> task.getStatus().equals(TaskStatus.TO_DO));
        return !allTasks.isEmpty();
    }

    /**
     * Retrieve id of given stage in Project. When Stage is added to the project it does not have any id yet.
     * After it is saved by repository to the database the Id is generated.
     *
     * @param project
     * @param stage
     * @return id of Stage
     */
    private Long getIdForCreatedStage(Project project, Stage stage) {
        return project.getStages().stream()
                .filter(projectStage -> projectStage.equals(stage))
                .findFirst()
                .map(AbstractEntity::getId).orElseThrow();
    }
}
