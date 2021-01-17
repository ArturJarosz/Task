package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageApplicationService;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.project.application.dto.StageBasicDto;
import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.StageDtoMapper;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationService
public class StageApplicationServiceImpl implements StageApplicationService {

    private ProjectQueryService projectQueryService;
    private ProjectValidator projectValidator;
    private ProjectRepository projectRepository;
    private StageValidator stageValidator;

    @Autowired
    public StageApplicationServiceImpl(ProjectQueryService projectQueryService,
                                       ProjectValidator projectValidator, ProjectRepository projectRepository,
                                       StageValidator stageValidator) {
        this.projectQueryService = projectQueryService;
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.stageValidator = stageValidator;
    }

    @Transactional
    @Override
    public CreatedEntityDto createStage(Long projectId,
                                        StageDto stageDto) {
        this.projectValidator.validateProjectExistence(projectId);
        StageValidator.validateCreateStageDto(stageDto);
        Stage stage = StageDtoMapper.INSTANCE.stageCreateDtoToStage(stageDto);
        Project project = this.projectRepository.load(projectId);
        project.addStage(stage);
        project = this.projectRepository.save(project);
        return new CreatedEntityDto(this.getIdForCreatedStage(project, stage));
    }

    @Override
    public void removeStage(Long projectId, Long stageId) {
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateStageExistence(stageId);
        Project project = this.projectRepository.load(projectId);
        project.removeStage(stageId);
    }

    @Override
    public void updateStage(Long projectId, Long stageId, StageDto stageDto) {
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateStageExistence(stageId);
        Project project = this.projectRepository.load(projectId);
        StageValidator.validateUpdateStageDto(stageDto);
        project.updateStage(stageId, stageDto.getName(), stageDto.getNote(), stageDto.getStageType(),
                stageDto.getDeadline());
        this.projectRepository.save(project);
    }

    @Override
    public StageDto getStage(Long stageId) {
        this.stageValidator.validateStageExistence(stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        return StageDtoMapper.INSTANCE.stageDtoFromStage(stage);
    }

    @Override
    public List<StageBasicDto> getStageBasicList(
            Long projectId) {
        this.projectValidator.validateProjectExistence(projectId);
        Project project = this.projectRepository.load(projectId);
        return project.getStages()
                .stream()
                .map(StageDtoMapper.INSTANCE::stageToStageBasicDto)
                .collect(Collectors.toList());
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
                .filter(projectCost -> projectCost.equals(stage))
                .findFirst()
                .map(AbstractEntity::getId).orElseThrow();
    }
}
