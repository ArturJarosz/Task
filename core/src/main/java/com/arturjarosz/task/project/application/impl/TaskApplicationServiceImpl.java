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
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationService
public class TaskApplicationServiceImpl implements TaskApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskApplicationServiceImpl.class);

    private final ProjectQueryService projectQueryService;
    private final ProjectRepository projectRepository;
    private final ProjectValidator projectValidator;
    private final StageValidator stageValidator;
    private final TaskDomainService taskDomainService;
    private final TaskValidator taskValidator;

    public TaskApplicationServiceImpl(ProjectQueryService projectQueryService, ProjectRepository projectRepository,
                                      ProjectValidator projectValidator, StageValidator stageValidator,
                                      TaskDomainService taskDomainService, TaskValidator taskValidator) {
        this.projectQueryService = projectQueryService;
        this.projectRepository = projectRepository;
        this.projectValidator = projectValidator;
        this.stageValidator = stageValidator;
        this.taskDomainService = taskDomainService;
        this.taskValidator = taskValidator;
    }

    @Transactional
    @Override
    public TaskDto createTask(Long projectId, Long stageId, TaskDto taskDto) {
        LOG.debug("Creating Task for Project with id {} and Stage with id {}", projectId, stageId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateCreateTaskDto(taskDto);
        Project project = this.projectRepository.load(projectId);
        Task task = this.taskDomainService.createTask(project, stageId, taskDto);
        project = this.projectRepository.save(project);
        LOG.debug("Task created.");
        return TaskDtoMapper.INSTANCE.taskToTaskDto(this.getNewTaskWithId(project, stageId, task));
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
    public TaskDto updateTask(Long projectId, Long stageId, Long taskId, TaskDto taskDto) {
        LOG.debug("Updating Task with id {}, from Stage with id {} on Project with id {}", taskId, stageId, projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        this.taskValidator.validateUpdateTaskDto(taskDto);
        Project project = this.projectRepository.load(projectId);
        TaskInnerDto taskInnerDto = TaskDtoMapper.INSTANCE.updateDtoToInnerDto(taskDto);
        Task task = this.taskDomainService.updateTask(project, stageId, taskId, taskInnerDto);
        this.projectRepository.save(project);
        LOG.debug("Task updated.");
        return TaskDtoMapper.INSTANCE.taskToTaskDto(task);
    }

    @Transactional
    @Override
    public TaskDto updateTaskStatus(Long projectId, Long stageId, Long taskId, TaskDto taskDto) {
        LOG.debug("Updating status on Task with id {}, from Stage with id {} on Project with id {}", taskId, stageId,
                projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        Project project = this.projectRepository.load(projectId);
        this.taskDomainService.updateTaskStatus(project, stageId, taskId, taskDto.getStatus());
        this.projectRepository.save(project);
        LOG.debug("Task status updated.");
        return TaskDtoMapper.INSTANCE.taskToTaskDto(this.getTaskById(project, stageId, taskId));
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
        return project.getStages().stream().filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .flatMap(stageOnProject -> stageOnProject.getTasks().stream())
                .map(TaskDtoMapper.INSTANCE::taskToTaskBasicDto).collect(Collectors.toList());

    }

    @Transactional
    @Override
    public TaskDto rejectTask(Long projectId, Long stageId, Long taskId) {
        LOG.debug("Rejecting Task with id {}", taskId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        Project project = this.projectRepository.load(projectId);
        this.taskDomainService.rejectTask(project, stageId, taskId);
        this.projectRepository.save(project);
        return TaskDtoMapper.INSTANCE.taskToTaskDto(this.getTaskById(project, stageId, taskId));
    }

    @Transactional
    @Override
    public TaskDto reopenTask(Long projectId, Long stageId, Long taskId) {
        LOG.debug("Reopening Task with id {}", taskId);
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
        Project project = this.projectRepository.load(projectId);
        this.taskDomainService.reopenTask(project, stageId, taskId);
        this.projectRepository.save(project);
        return TaskDtoMapper.INSTANCE.taskToTaskDto(this.getTaskById(project, stageId, taskId));
    }

    private Task getNewTaskWithId(Project project, Long stageId, Task task) {
        Predicate<Stage> stagePredicate = stage -> stage.getId().equals(stageId);
        Predicate<Task> taskPredicate = taskOnStage -> taskOnStage.equals(task);
        return project.getStages().stream().filter(stagePredicate).flatMap(stage -> stage.getTasks().stream())
                .filter(taskPredicate).findFirst().orElse(null);
    }

    private Task getTaskById(Project project, Long stageId, Long taskId) {
        Predicate<Stage> stagePredicate = stage -> stage.getId().equals(stageId);
        Predicate<Task> taskPredicate = taskOnStage -> taskOnStage.getId().equals(taskId);
        return project.getStages().stream().filter(stagePredicate).flatMap(stage -> stage.getTasks().stream())
                .filter(taskPredicate).findFirst().orElse(null);
    }
}
