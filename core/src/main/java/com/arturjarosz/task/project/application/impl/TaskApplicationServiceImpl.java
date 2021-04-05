package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.project.application.TaskApplicationService;
import com.arturjarosz.task.project.application.TaskValidator;
import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.application.mapper.TaskDtoMapper;
import com.arturjarosz.task.project.domain.TaskDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.project.status.domain.TaskWorkflowService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationService
public class TaskApplicationServiceImpl implements TaskApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskApplicationService.class);

    private final ProjectQueryService projectQueryService;
    private final ProjectRepository projectRepository;
    private final ProjectValidator projectValidator;
    private final StageValidator stageValidator;
    private final TaskDomainService taskDomainService;
    private final TaskWorkflowService taskWorkflowService;
    private final TaskValidator taskValidator;

    public TaskApplicationServiceImpl(ProjectQueryService projectQueryService,
                                      ProjectRepository projectRepository, ProjectValidator projectValidator,
                                      StageValidator stageValidator,
                                      TaskDomainService taskDomainService,
                                      TaskWorkflowService taskWorkflowService,
                                      TaskValidator taskValidator) {
        this.projectQueryService = projectQueryService;
        this.projectRepository = projectRepository;
        this.projectValidator = projectValidator;
        this.stageValidator = stageValidator;
        this.taskDomainService = taskDomainService;
        this.taskWorkflowService = taskWorkflowService;
        this.taskValidator = taskValidator;
    }

    @Transactional
    @Override
    public CreatedEntityDto createTask(Long projectId, Long stageId,
                                       TaskDto taskDto) {
        LOG.debug("Creating Task for Project with id {} and Stage with id {}", projectId, stageId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        TaskValidator.validateCreateTaskDto(taskDto);
        Task task = this.taskDomainService.createTask(taskDto);
        Project project = this.projectRepository.load(projectId);
        project.addTaskToStage(stageId, task);
        project = this.projectRepository.save(project);
        LOG.debug("Task created.");
        return new CreatedEntityDto(this.getCreatedTaskId(stageId, project, task));
    }

    @Transactional
    @Override
    public void deleteTask(Long projectId, Long stageId, Long taskId) {
        LOG.debug("Removing Task with id {}, from Stage with id {} on Project with id {}", taskId, stageId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        Project project = this.projectRepository.load(projectId);
        project.removeTaskFromStage(stageId, taskId);
        this.projectRepository.save(project);
        LOG.debug("Task removed.");
    }

    @Transactional
    @Override
    public void updateTask(Long projectId, Long stageId, Long taskId,
                           TaskDto taskDto) {
        LOG.debug("Updating Task with id {}, from Stage with id {} on Project with id {}", taskId, stageId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        Project project = this.projectRepository.load(projectId);
        TaskInnerDto taskInnerDto = TaskDtoMapper.INSTANCE.updateDtoToInnerDto(taskDto);
        project.updateTaskOnStage(stageId, taskId, taskInnerDto);
        this.projectRepository.save(project);
        LOG.debug("Task updated.");
    }

    @Transactional
    @Override
    public void updateTaskStatus(Long projectId, Long stageId, Long taskId,
                                 TaskDto taskDto) {
        LOG.debug("Updating status on Task with id {}, from Stage with id {} on Project with id {}", taskId, stageId,
                projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        Project project = this.projectRepository.load(projectId);
        this.taskWorkflowService
                .changeTaskStatusOnProject(project, stageId, taskId, taskDto.getStatus());
        this.projectRepository.save(project);
        LOG.debug("Task status updated.");
    }

    @Override
    public TaskDto getTask(Long projectId, Long stageId, Long taskId) {
        LOG.debug("Loading Task with id {}, from Stage with id {} on Project with id {}", taskId, stageId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        return this.projectQueryService.getTaskByTaskId(taskId);
    }

    @Override
    public List<TaskDto> getTaskList(Long projectId, Long stageId) {
        LOG.debug("Loading list of Tasks for Stage with id {} on Project with id {}", stageId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Project project = this.projectRepository.load(projectId);
        return project.getStages().stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .flatMap(stageOnProject -> stageOnProject.getTasks().stream())
                .map(TaskDtoMapper.INSTANCE::taskToTaskBasicDto)
                .collect(Collectors.toList());

    }

    /**
     * Retrieve id of given Task in Stage in Project. When Task is added to the Project it does not have any id yet.
     * After it is saved by repository to the database the Id is generated.
     *
     * @param stageId
     * @param project
     * @param task
     * @return id of Task
     */
    private Long getCreatedTaskId(Long stageId, Project project, Task task) {
        Stage stage = project.getStages().stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId)).findFirst().orElseThrow();
        return stage.getTasks().stream()
                .filter(taskOnStage -> taskOnStage.equals(task))
                .findFirst()
                .map(taskOnStage -> taskOnStage.getId())
                .orElseThrow();
    }
}
