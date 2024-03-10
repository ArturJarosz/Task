package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.contract.status.validator.ContractWorkflowValidator
import com.arturjarosz.task.dto.StageDto
import com.arturjarosz.task.dto.StageTypeDto
import com.arturjarosz.task.finance.application.InstallmentApplicationService
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.StageValidator
import com.arturjarosz.task.project.domain.StageDomainService
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.project.status.task.TaskStatus
import com.arturjarosz.task.utils.ProjectBuilder
import com.arturjarosz.task.utils.StageBuilder
import com.arturjarosz.task.utils.TaskBuilder
import spock.lang.Specification

import java.time.LocalDate

class StageApplicationServiceImplTest extends Specification {
    static final Long PROJECT_ID = 1l
    static final Long PROJECT_WITH_STAGE_ID = 2L
    static final Long STAGE_ID = 5L
    static final Long STAGE_WITH_TASKS_IN_TODO_ID = 10L
    static final Long STAGE_WITH_TASKS_IN_DIFFERENT_STATUSES = 20L
    static final Long INSTALLMENT_ID = 100L
    static final String NEW_STAGE_NAME = "newStageName"
    static final String NEW_STAGE_NOTE = "newStageNote"
    static final StageTypeDto NEW_STAGE_TYPE = StageTypeDto.FUNCTIONAL_LAYOUT
    static final LocalDate NEW_DEADLINE_DATE = LocalDate.of(2022, 12, 12)

    def projectQueryService = Mock(ProjectQueryServiceImpl)
    def projectValidator = Mock(ProjectValidator)
    def projectRepository = Mock(ProjectRepository)
    def stageValidator = Mock(StageValidator)
    def stageDomainService = Mock(StageDomainService)
    def contractWorkflowValidator = Mock(ContractWorkflowValidator)
    def installmentApplicationService = Mock(InstallmentApplicationService)

    def stageApplicationService = new StageApplicationServiceImpl(projectQueryService, projectValidator,
            projectRepository, stageDomainService, stageValidator, contractWorkflowValidator, installmentApplicationService)

    def "createStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad()
            def stageDto = new StageDto()
            this.mockProjectRepositorySaveProjectWithStage()
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_ID)
    }

    def "createStage should call validateCreateStageDto on stageValidator"() {
        given:
            this.mockProjectRepositoryLoad()
            def stageDto = new StageDto()
            this.mockProjectRepositorySaveProjectWithStage()
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto)
        then:
            1 * this.stageValidator.validateCreateStageDto(_ as StageDto)
    }

    def "createStage should call validateContractAllowsForWorkObjectsCreation on contractWorkflowValidator"() {
        given:
            this.mockProjectRepositoryLoad()
            def stageDto = new StageDto()
            this.mockProjectRepositorySaveProjectWithStage()
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto)
        then:
            1 * this.contractWorkflowValidator.validateContractAllowsForWorkObjectsCreation(PROJECT_ID)
    }

    def "createStage should load project from projectRepository"() {
        given:
            def stageDto = new StageDto()
            this.mockProjectRepositorySaveProjectWithStage()
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto)
        then:
            1 * this.projectRepository.findById(PROJECT_ID) >> Optional.of(this.prepareProject())
    }

    def "createStage should call createStage on stageDomainService"() {
        given:
            this.mockProjectRepositoryLoad()
            def stageDto = new StageDto()
            this.mockProjectRepositorySaveProjectWithStage()
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto)
        then:
            1 * this.stageDomainService.createStage(_ as Project, _ as StageDto)
    }

    def "createStage should save project with repository"() {
        given:
            this.mockProjectRepositoryLoad()
            def stageDto = new StageDto()
        when:
            this.stageApplicationService.createStage(PROJECT_ID, stageDto)
        then:
            1 * this.projectRepository.save(_ as Project) >> this.prepareProjectWithStage()
    }

    def "removeStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_WITH_STAGE_ID)
    }

    def "removeStage should call validateExistenceOfStageInProject"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_ID)
    }

    def "removeStage should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.projectRepository.findById(PROJECT_WITH_STAGE_ID) >> Optional.of(this.prepareProjectWithStage())
    }

    def "removeStage should save project with projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.projectRepository.save(_ as Project)
    }

    def "removeStage should remove stage from project"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.projectRepository.save({ Project project -> project.stages.size() == 0
            })
    }

    def "removeStage should also remove related installment"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
            this.mockLoadRelatedInstallmentId()
        when:
            this.stageApplicationService.removeStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.installmentApplicationService.removeInstallment(PROJECT_WITH_STAGE_ID, INSTALLMENT_ID)
    }

    def "updateStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
            def stageDto = this.prepareStageDtoForUpdate()
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_WITH_STAGE_ID)
    }

    def "updateStage should call validateExistenceOfStageInProject"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
            def stageDto = this.prepareStageDtoForUpdate()
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_ID)
    }

    def "updateStage should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
            def stageDto = this.prepareStageDtoForUpdate()
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto)
        then:
            1 * this.projectRepository.findById(PROJECT_WITH_STAGE_ID) >> Optional.of(this.prepareProjectWithStage())
    }

    def "updateStage should call validateUpdateStageDto from stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
            def stageDto = this.prepareStageDtoForUpdate()
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto)
        then:
            1 * this.stageValidator.validateUpdateStageDto(_ as StageDto)
    }

    def "updateStage should save project with projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
            def stageDto = this.prepareStageDtoForUpdate()
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto)
        then:
            1 * this.projectRepository.save(_ as Project)
    }

    def "updateStage should call updateStage on stageDomainService"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
            def stageDto = this.prepareStageDtoForUpdate()
        when:
            this.stageApplicationService.updateStage(PROJECT_WITH_STAGE_ID, STAGE_ID, stageDto)
        then:
            1 * this.stageDomainService.updateStage(_ as Project, STAGE_ID, _ as StageDto)
    }

    def "getStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectQueryServiceGetStageById()
        when:
            this.stageApplicationService.getStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_STAGE_ID)
    }

    def "getStage should call validateExistenceStageInProject on stageValidator"() {
        given:
            this.mockProjectQueryServiceGetStageById()
        when:
            this.stageApplicationService.getStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_ID)
    }

    def "getStage get stage from projectQueryService"() {
        given:
            this.mockProjectQueryServiceGetStageById()
        when:
            this.stageApplicationService.getStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.projectQueryService.getStageById(STAGE_ID) >> this.prepareStage()
    }

    def "getStageListForProject should call validateProjectExistence"() {
        given:
            this.mockProjectQueryServiceGetStagesForProjectById()
        when:
            this.stageApplicationService.getStageListForProject(PROJECT_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID)
    }

    def "getStageListForProject should load all stages for project from projectQueryService"() {
        given:
            this.mockProjectQueryServiceGetStagesForProjectById()
        when:
            List<StageDto> stages = this.stageApplicationService.getStageListForProject(PROJECT_ID)
        then:
            1 * this.projectQueryService.getStagesForProjectById(PROJECT_ID) >> this.prepareStageDtoList()
            stages.size() == 1
    }

    def "rejectStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_WITH_STAGE_ID)
    }

    def "rejectStage should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.stageValidator.validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_ID)
    }

    def "rejectStage should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.projectRepository.findById(PROJECT_WITH_STAGE_ID) >> Optional.of(this.prepareProjectWithStage())
    }

    def "rejectStage should call rejectStage on stageDomainService"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.stageDomainService.rejectStage(_ as Project, STAGE_ID)
    }

    def "rejectStage should save project with save method on projectRepository"() {
        given:
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.rejectStage(PROJECT_WITH_STAGE_ID, STAGE_ID)
        then:
            1 * this.projectRepository.save(_ as Project)
    }

    def "reopenStage should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectQueryServiceGetStageByIdWithTasks()
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_WITH_STAGE_ID)
    }

    def "reopenStage should call validateExistenceOfStageInProject on stageValidator"() {
        given:
            this.mockProjectQueryServiceGetStageByIdWithTasks()
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID)
        then:
            1 * this.stageValidator.
                    validateExistenceOfStageInProject(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID)
    }

    def "reopenStage should load project from project repository"() {
        given:
            this.mockProjectQueryServiceGetStageByIdWithTasks()
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID)
        then:
            1 * this.projectRepository.findById(PROJECT_WITH_STAGE_ID) >> Optional.of(this.prepareProjectWithStage())
    }

    def "reopenStage should call reopenStage on stageDomainService"() {
        given:
            this.mockProjectQueryServiceGetStageByIdWithTasks()
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID)
        then:
            1 * this.stageDomainService.reopenStage(_ as Project, STAGE_WITH_TASKS_IN_TODO_ID)
    }

    def "reopenStage should save project with projectRepository"() {
        given:
            this.mockProjectQueryServiceGetStageByIdWithTasks()
            this.mockProjectRepositoryLoadProjectWithStage()
        when:
            this.stageApplicationService.reopenStage(PROJECT_WITH_STAGE_ID, STAGE_WITH_TASKS_IN_TODO_ID)
        then:
            1 * this.projectRepository.save(_ as Project)
    }

    // mocks
    private void mockProjectRepositoryLoad() {
        this.projectRepository.findById(PROJECT_ID) >> Optional.of(this.prepareProject())
    }

    private void mockProjectRepositoryLoadProjectWithStage() {
        this.projectRepository.findById(PROJECT_WITH_STAGE_ID) >> Optional.of(this.prepareProjectWithStage())
    }

    private void mockLoadRelatedInstallmentId() {
        this.projectQueryService.getInstallmentIdForStage(_ as Long) >> INSTALLMENT_ID
    }

    private void mockProjectQueryServiceGetStageById() {
        this.projectQueryService.getStageById(STAGE_ID) >> this.prepareStage()
    }

    private void mockProjectQueryServiceGetStagesForProjectById() {
        ArrayList<StageDto> stages = prepareStageDtoList()
        this.projectQueryService.getStagesForProjectById(PROJECT_ID) >> stages
    }

    private void mockProjectQueryServiceGetStageByIdWithTasks() {
        this.projectQueryService.getStageById(STAGE_WITH_TASKS_IN_TODO_ID) >> this
                .prepareStageWithTaskOnlyInToDoStatuses()
        this.projectQueryService.getStageById(STAGE_WITH_TASKS_IN_DIFFERENT_STATUSES) >> this
                .prepareSageWithTasksInDifferentStatuses()
    }

    private void mockProjectRepositorySaveProjectWithStage() {
        1 * this.projectRepository.save(_ as Project) >> this.prepareProjectWithStage()
    }

    // preparing parameters
    private Project prepareProject() {
        return new ProjectBuilder().withId(PROJECT_ID).build()
    }

    private Project prepareProjectWithStage() {
        return new ProjectBuilder()
                .withId(PROJECT_WITH_STAGE_ID)
                .withStage(this.prepareStage())
                .build()
    }

    private Stage prepareStage() {
        return new StageBuilder().withId(STAGE_ID).withStatus(StageStatus.TO_DO).build()
    }

    private StageDto prepareStageDtoForUpdate() {
        StageDto stageDto = new StageDto(name: NEW_STAGE_NAME, note: NEW_STAGE_NOTE, type: NEW_STAGE_TYPE,
                deadline: NEW_DEADLINE_DATE)
        return stageDto
    }

    private ArrayList<StageDto> prepareStageDtoList() {
        List<StageDto> stages = new ArrayList<>()
        StageDto stageDto = Mock(StageDto)
        stages.add(stageDto)
        return stages
    }

    private Stage prepareStageWithTaskOnlyInToDoStatuses() {
        Task task = this.prepareTaskWithStatus(TaskStatus.TO_DO)
        Task anotherTask = this.prepareTaskWithStatus(TaskStatus.TO_DO)
        List<Task> tasks = new ArrayList<>()
        tasks.add(task)
        tasks.add(anotherTask)
        return new StageBuilder().withId(STAGE_WITH_TASKS_IN_TODO_ID).withTasks(tasks).build()
    }

    private Stage prepareSageWithTasksInDifferentStatuses() {
        Task task = this.prepareTaskWithStatus(TaskStatus.TO_DO)
        Task anotherTask = this.prepareTaskWithStatus(TaskStatus.IN_PROGRESS)
        List<Task> tasks = new ArrayList<>()
        tasks.add(task)
        tasks.add(anotherTask)
        return new StageBuilder().withId(STAGE_WITH_TASKS_IN_DIFFERENT_STATUSES).withTasks(tasks).build()
    }

    private Task prepareTaskWithStatus(TaskStatus taskStatus) {
        return new TaskBuilder().withStatus(taskStatus).build()
    }
}
