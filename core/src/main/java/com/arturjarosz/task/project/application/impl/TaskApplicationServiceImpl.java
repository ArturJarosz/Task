package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.contract.status.validator.ContractWorkflowValidator;
import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.dto.UpdateStatusRequestDto;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.project.application.TaskApplicationService;
import com.arturjarosz.task.project.application.TaskValidator;
import com.arturjarosz.task.project.application.mapper.TaskMapper;
import com.arturjarosz.task.project.domain.TaskDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class TaskApplicationServiceImpl implements TaskApplicationService {

    private final ProjectQueryService projectQueryService;
    private final ProjectRepository projectRepository;
    private final ProjectValidator projectValidator;
    private final StageValidator stageValidator;
    private final TaskDomainService taskDomainService;
    private final TaskValidator taskValidator;
    private final ContractWorkflowValidator contractWorkflowValidator;
    private final TaskMapper taskMapper;

    @Transactional
    @Override
    public TaskDto createTask(Long projectId, Long stageId, TaskDto taskDto) {
        LOG.debug("Creating Task for Project with id {} and Stage with id {}", projectId, stageId);

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateCreateTaskDto(taskDto);
        this.contractWorkflowValidator.validateContractAllowsForWorkObjectsCreation(projectId);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);
        var task = this.taskDomainService.createTask(project, stageId, taskDto);
        project = this.projectRepository.save(project);
        LOG.debug("Task created.");

        return this.taskMapper.taskToTaskDto(this.getNewTaskWithId(project, stageId, task));
    }

    @Transactional
    @Override
    public void deleteTask(Long projectId, Long stageId, Long taskId) {
        LOG.debug("Removing Task with id {}, from Stage with id {} on Project with id {}", taskId, stageId, projectId);

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);
        project.removeTaskFromStage(stageId, taskId);
        this.projectRepository.save(project);

        LOG.debug("Task removed.");
    }

    @Transactional
    @Override
    public TaskDto updateTask(Long projectId, Long stageId, Long taskId, TaskDto taskDto) {
        LOG.debug("Updating Task with id {}, from Stage with id {} on Project with id {}", taskId, stageId, projectId);

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        this.taskValidator.validateUpdateTaskDto(taskDto);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);
        var taskInnerDto = this.taskMapper.updateDtoToInnerDto(taskDto);
        var task = this.taskDomainService.updateTask(project, stageId, taskId, taskInnerDto);
        this.projectRepository.save(project);
        LOG.debug("Task updated.");
        return this.taskMapper.taskToTaskDto(task);
    }

    @Transactional
    @Override
    public TaskDto updateTaskStatus(Long projectId, Long stageId, Long taskId,
            UpdateStatusRequestDto statusRequestDto) {
        LOG.debug("Updating status on Task with id {}, from Stage with id {} on Project with id {}", taskId, stageId,
                projectId);
        var maybeProject = this.projectRepository.findById(projectId);

        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);
        this.taskDomainService.updateTaskStatus(project, stageId, taskId,
                TaskStatus.valueOf(statusRequestDto.getStatus().name()));
        this.projectRepository.save(project);

        LOG.debug("Task status updated.");
        return this.taskMapper.taskToTaskDto(this.getTaskById(project, stageId, taskId));
    }

    @Transactional
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

        var maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        var project = maybeProject.orElseThrow(ResourceNotFoundException::new);

        return project.getStages()
                .stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .flatMap(stageOnProject -> stageOnProject.getTasks().stream())
                .map(this.taskMapper::taskToTaskDto)
                .toList();

    }

    private Task getNewTaskWithId(Project project, Long stageId, Task task) {
        Predicate<Stage> stagePredicate = stage -> stage.getId().equals(stageId);
        Predicate<Task> taskPredicate = taskOnStage -> taskOnStage.equals(task);
        return project.getStages()
                .stream()
                .filter(stagePredicate)
                .flatMap(stage -> stage.getTasks().stream())
                .filter(taskPredicate)
                .findFirst()
                .orElse(null);
    }

    private Task getTaskById(Project project, Long stageId, Long taskId) {
        Predicate<Stage> stagePredicate = stage -> stage.getId().equals(stageId);
        Predicate<Task> taskPredicate = taskOnStage -> taskOnStage.getId().equals(taskId);
        return project.getStages()
                .stream()
                .filter(stagePredicate)
                .flatMap(stage -> stage.getTasks().stream())
                .filter(taskPredicate)
                .findFirst()
                .orElse(null);
    }
}
