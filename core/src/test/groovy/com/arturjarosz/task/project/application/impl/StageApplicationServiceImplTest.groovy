package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.StageValidator
import com.arturjarosz.task.project.application.dto.StageDto
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.StageType
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.status.stage.StageStatusTransition
import com.arturjarosz.task.project.status.stage.StageWorkflow
import com.arturjarosz.task.project.status.stage.impl.StageWorkflowServiceImpl
import com.arturjarosz.task.project.status.task.TaskStatus
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.project.utils.StageBuilder
import com.arturjarosz.task.project.utils.TaskBuilder
import spock.lang.Specification

import java.time.LocalDate

class StageApplicationServiceImplTest extends Specification {
    private static final Long PROJECT_ID = 1l;
    private static final Long PROJECT_WITH_STAGE_ID = 2L;
    private static final Long STAGE_ID = 5L;
    private static final Long STAGE_WITH_TASKS_IN_TODO_ID = 10L;
    private static final Long STAGE_WITH_TASKS_IN_DIFFERENT_STATUSES = 20L;
    private static final String NEW_STAGE_NAME = "newStageName";
    private static final String NEW_STAGE_NOTE = "newStageNote";
    private static final StageType NEW_STAGE_TYPE = StageType.FUNCTIONAL_LAYOUT;
    private static final LocalDate NEW_DEADLINE_DATE = LocalDate.of(2022, 12, 12);

    def projectQueryService = Mock(ProjectQueryServiceImpl);
    def projectValidator = Mock(ProjectValidator);
    def projectRepository = Mock(ProjectRepositoryImpl);
    def stageValidator = Mock(StageValidator);
    def stageWorkflow = Mock(StageWorkflow);
    def stageWorkflowService = Mock(StageWorkflowServiceImpl);

    def stageApplicationService = new StageApplicationServiceImpl(projectQueryService, projectValidator,
            projectRepository, stageValidator, stageWorkflow, stageWorkflowService);

    def "createStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad();
            StageDto stageDto = new StageDto();
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "createStage should call validateCreateStageDto on stageValidator"() {
        given:
            this.mockProjectRepositoryLoad();
            StageDto stageDto = new StageDto();
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto);
        then:
            1 * this.stageValidator.validateCreateStageDto(_ as StageDto);
    }

    def "createStage should load project from projectRepository"() {
        given:
            StageDto stageDto = new StageDto();
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto);
        then:
            1 * this.projectRepository.load(PROJECT_ID) >> this.prepareProject();
    }

    def "createStage should add stage to project"() {
        given:
            this.mockProjectRepositoryLoad();
            StageDto stageDto = new StageDto();
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto);
        then:
            1 * this.projectRepository.save({
                Project project -> project.getStages().size() == 1;
            })
    }

    def "createStage should save project with repository"() {
        given:
            this.mockProjectRepositoryLoad();
            StageDto stageDto = new StageDto();
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto);
        then:
            1 * this.projectRepository.save(_ as Project);
    }

    def "removeStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_STAGE_ID);
    }

    def "removeStage should call validateExistenceOfStageInProject"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_ID);
    }

    def "removeStage should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.projectRepository.load(PROJECT_WITH_STAGE_ID) >> this.prepareProjectWithStage();
    }

    def "removeStage should save project with projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.projectRepository.save(_ as Project);
    }

    def "removeStage should remove stage from project"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.getStages().size() == 0;
            });
    }

    def "updateStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
            StageDto stageDto = this.prepareStageDtoForUpdate();
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_STAGE_ID);
    }

    def "updateStage should call validateExistenceOfStageInProject"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
            StageDto stageDto = this.prepareStageDtoForUpdate();
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_ID);
    }

    def "updateStage should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
            StageDto stageDto = this.prepareStageDtoForUpdate();
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto);
        then:
            1 * this.projectRepository.load(PROJECT_WITH_STAGE_ID) >> this.prepareProjectWithStage();
    }

    def "updateStage should call validateUpdateStageDto from stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
            StageDto stageDto = this.prepareStageDtoForUpdate();
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto);
        then:
            1 * this.stageValidator.validateUpdateStageDto(_ as StageDto);
    }

    def "updateStage should save project with projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
            StageDto stageDto = this.prepareStageDtoForUpdate();
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto);
        then:
            1 * this.projectRepository.save(_ as Project);
    }

    def "updateStage should update data on stage"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
            StageDto stageDto = this.prepareStageDtoForUpdate();
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    Stage stage = project.getStages().iterator().next();
                    stage.getName() == NEW_STAGE_NAME;
                    stage.getNote() == NEW_STAGE_NOTE;
                    stage.getStageType() == NEW_STAGE_TYPE;
                    stage.getDeadline() == NEW_DEADLINE_DATE;
            });
    }

    def "getStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectQueryServiceGetStageById();
        when:
            this.stageApplicationService.getStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_STAGE_ID);
    }

    def "getStage should call validateExistenceStageInProject on stageValidator"() {
        given:
            this.mockProjectQueryServiceGetStageById();
        when:
            this.stageApplicationService.getStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_ID);
    }

    def "getStage get stage from projectQueryService"() {
        given:
            this.mockProjectQueryServiceGetStageById();
        when:
            this.stageApplicationService.getStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.projectQueryService.getStageById(STAGE_ID) >> this.prepareStage();
    }

    def "getStageListForProject should call validateProjectExistence"() {
        given:
            this.mockProjectQueryServiceGetStagesForProjectById();
        when:
            this.stageApplicationService.getStageListForProject(PROJECT_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "getStageListForProject should load all stages for project from projectQueryService"() {
        given:
            this.mockProjectQueryServiceGetStagesForProjectById();
        when:
            List<StageDto> stages = this.stageApplicationService.getStageListForProject(PROJECT_ID);
        then:
            1 * this.projectQueryService.getStagesForProjectById(PROJECT_ID) >> this.prepareStageDtoList();
            stages.size() == 1;
    }

    def "rejectStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_STAGE_ID);
    }

    def "rejectStage should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_ID);
    }

    def "rejectStage should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.projectRepository.load(PROJECT_WITH_STAGE_ID) >> this.prepareProjectWithStage();
    }

    def "rejectStage should call changeStageStatusOnProject on stageWorkflowService with REJECT_FROM_IN_PROGRESS"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.stageWorkflowService.
                    changeStageStatusOnProject(_ as Project, STAGE_ID, StageStatusTransition.REJECT_FROM_IN_PROGRESS);
    }

    def "rejectStage should save project with save method on projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID);
        then:
            1 * this.projectRepository.save(_ as Project);
    }

    def "reopenStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectQueryServiceGetStageByIdWithTasks();
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_STAGE_ID);
    }

    def "reopenStage should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            this.mockProjectQueryServiceGetStageByIdWithTasks();
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID);
        then:
            1 * this.stageValidator.
                    validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID);
    }

    def "reopenStage should load project from project repository"() {
        given:
            this.mockProjectQueryServiceGetStageByIdWithTasks();
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID);
        then:
            1 * this.projectRepository.load(PROJECT_WITH_STAGE_ID) >> this.prepareProjectWithStage();
    }

    def "reopenStage should call changeStageStatusOnProject with TO_DO when stage has tasks only in TO_DO"() {
        given:
            //TODO: fix test
            this.mockProjectQueryServiceGetStageByIdWithTasks();
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID);
        then:
            1 * this.stageWorkflowService.
                    changeStageStatusOnProject(_ as Project, STAGE_WITH_TASKS_IN_TODO_ID, StageStatusTransition.REOPEN)
    }

    def "reopenStage should call changeStageStatusOnProject with IN_PROGRESS on stage has tasks in different statuses"() {
        given:
            //TODO: fix test
            this.mockProjectQueryServiceGetStageByIdWithTasks();
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_DIFFERENT_STATUSES);
        then:
            1 * this.stageWorkflowService.
                    changeStageStatusOnProject(_ as Project, STAGE_WITH_TASKS_IN_DIFFERENT_STATUSES,
                            StageStatusTransition.REOPEN);
    }

    def "reopenStage should save project with projectRepository"() {
        given:
            this.mockProjectQueryServiceGetStageByIdWithTasks();
            this.mockProjectRepositoryLoadProjectWithStage();
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID);
        then:
            1 * this.projectRepository.save(_ as Project);
    }


    // mocks
    private void mockProjectRepositoryLoad() {
        this.projectRepository.load(PROJECT_ID) >> this.prepareProject();
    }

    private void mockProjectRepositoryLoadProjectWithStage() {
        this.projectRepository.load(PROJECT_WITH_STAGE_ID) >> this.prepareProjectWithStage();
    }

    private void mockProjectQueryServiceGetStageById() {
        this.projectQueryService.getStageById(STAGE_ID) >> this.prepareStage();
    }

    private void mockProjectQueryServiceGetStagesForProjectById() {
        ArrayList<StageDto> stages = prepareStageDtoList()
        this.projectQueryService.getStagesForProjectById(PROJECT_ID) >> stages;
    }

    private void mockProjectQueryServiceGetStageByIdWithTasks() {
        this.projectQueryService.getStageById(STAGE_WITH_TASKS_IN_TODO_ID) >> this
                .prepareStageWithTaskOnlyInToDoStatuses();
        this.projectQueryService.getStageById(STAGE_WITH_TASKS_IN_DIFFERENT_STATUSES) >> this
                .prepareSageWithTasksInDifferentStatuses();
    }

    // preparing parameters
    private Project prepareProject() {
        return new ProjectBuilder().withId(PROJECT_ID).build();
    }

    private Project prepareProjectWithStage() {
        return new ProjectBuilder()
                .withId(PROJECT_WITH_STAGE_ID)
                .withStage(this.prepareStage())
                .build();
    }

    private Stage prepareStage() {
        return new StageBuilder().withId(STAGE_ID).build();
    }

    private StageDto prepareStageDtoForUpdate() {
        StageDto stageDto = new StageDto();
        stageDto.setName(NEW_STAGE_NAME);
        stageDto.setNote(NEW_STAGE_NOTE);
        stageDto.setStageType(NEW_STAGE_TYPE);
        stageDto.setDeadline(NEW_DEADLINE_DATE);
        return stageDto;
    }

    private ArrayList<StageDto> prepareStageDtoList() {
        List<StageDto> stages = new ArrayList<>();
        StageDto stageDto = Mock(StageDto);
        stages.add(stageDto);
        return stages;
    }

    private Stage prepareStageWithTaskOnlyInToDoStatuses() {
        Task task = this.prepareTaskWithStatus(TaskStatus.TO_DO);
        Task anotherTask = this.prepareTaskWithStatus(TaskStatus.TO_DO);
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);
        tasks.add(anotherTask);
        return new StageBuilder().withId(STAGE_WITH_TASKS_IN_TODO_ID).withTasks(tasks).build();
    }

    private Stage prepareSageWithTasksInDifferentStatuses() {
        Task task = this.prepareTaskWithStatus(TaskStatus.TO_DO);
        Task anotherTask = this.prepareTaskWithStatus(TaskStatus.IN_PROGRESS);
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);
        tasks.add(anotherTask);
        return new StageBuilder().withId(STAGE_WITH_TASKS_IN_DIFFERENT_STATUSES).withTasks(tasks).build();
    }

    private Task prepareTaskWithStatus(TaskStatus taskStatus) {
        return new TaskBuilder().withStatus(taskStatus).build();
    }
}
