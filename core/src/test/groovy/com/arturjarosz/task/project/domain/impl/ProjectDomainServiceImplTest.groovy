package com.arturjarosz.task.project.domain.impl

import com.arturjarosz.task.project.application.dto.ProjectContractDto
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.project.domain.ProjectDataValidator
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.project.status.project.impl.ProjectWorkflowServiceImpl
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.project.utils.StageBuilder
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import spock.lang.Specification

import java.time.LocalDate

class ProjectDomainServiceImplTest extends Specification {
    private static final String NAME = "projectName";
    private static final String NEW_NAME = "newProjectName";
    private static final String NEW_NOTE = "newNote";
    private static final String STAGE_NAME = "stageName";
    private static final Long ARCHITECT_ID = 100L;
    private static final Long CLIENT_ID = 1000L;
    private static final Long PROJECT_ID = 10L;

    def projectDataValidator = Mock(ProjectDataValidator);
    def projectRepository = Mock(ProjectRepositoryImpl);
    def projectWorkflow = Mock(ProjectWorkflow);
    def projectWorkflowService = Mock(ProjectWorkflowServiceImpl);

    def projectDomainService = new ProjectDomainServiceImpl(projectDataValidator, projectRepository, projectWorkflow,
            projectWorkflowService);

    def "createProject should call changeProject on projectWorkflowService"() {
        given:
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto();
        when:
            Project project = this.projectDomainService.createProject(projectCreateDto);
        then:
            1 * this.projectWorkflowService.changeProjectStatus(_, _);
    }

    def "createProject should return newly created project with"() {
        given:
            this.mockProjectWorkflow();
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto();
        when:
            Project project = this.projectDomainService.createProject(projectCreateDto);
        then:
            1 * this.projectWorkflowService.changeProjectStatus({
                Project createdProject ->
                    TestUtils.setFieldForObject(createdProject, "status", ProjectStatus.OFFER);
            }, _) >> {}
            project.getName() == projectCreateDto.getName();
            project.getStatus() == ProjectStatus.OFFER;
    }

    def "updateProject should change data on project and return updated instance"() {
        given:
            ProjectDto projectDto = this.prepareUpdateProjectDto();
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER);
        when:
            Project updatedProject = this.projectDomainService.updateProject(project, projectDto);
        then:
            updatedProject.getName() == NEW_NAME;
            updatedProject.getNote() == NEW_NOTE;
    }

    def "signProject should call signingDateNotInFuture from projectDataValidator"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER);
            ProjectContractDto projectContractDto = this.prepareProjectContractDto();
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project, projectContractDto);
        then:
            1 * this.projectDataValidator.signingDateNotInFuture(_);
    }

    def "signProject should call startDateNotBeforeSigningDate from projectDataValidator"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER);
            ProjectContractDto projectContractDto = this.prepareProjectContractDto();
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project, projectContractDto);
        then:
            1 * this.projectDataValidator.startDateNotBeforeSigningDate(_, _);
    }

    def "signProject should call deadlineNotBeforeStartDate from projectDataValidator"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER);
            ProjectContractDto projectContractDto = this.prepareProjectContractDto();
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project, projectContractDto);
        then:
            1 * this.projectDataValidator.deadlineNotBeforeStartDate(_, _);
    }

    def "signProject should call signContract on project"() {
        given:
            Project project = Mock(Project);
            ProjectContractDto projectContractDto = this.prepareProjectContractDto();
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project, projectContractDto);
        then:
            1 * project.signContract(_, _, _);
    }

    def "signProject should call changeProjectStatus on projectWorkflowService"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER);
            ProjectContractDto projectContractDto = this.prepareProjectContractDto();
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project, projectContractDto);
        then:
            1 * this.projectWorkflowService.changeProjectStatus(_, ProjectStatus.TO_DO);
    }

    def "finishProject should set endDate to today, if endDate is not provided"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.IN_PROGRESS);
            LocalDate today = LocalDate.now();
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            finishedProject.getEndDate() == today;
    }

    def "finishProject should call endDateNotBeforeStartDate on projectDataValidator"() {
        given:
            Project project = this.prepareProjectInProgressWithStatus();
            LocalDate today = LocalDate.now();
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * this.projectDataValidator.endDateNotBeforeStartDate(_ as LocalDate, _ as LocalDate);
    }

    def "finishProject should call finishProject on project"() {
        given:
            Project project = Mock(Project);
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * project.finishProject(_);
    }

    def "finishProject should call changeProjectStatus on projectWorkflowService"() {
        given:
            Project project = this.prepareProjectInProgressWithStatus();
            LocalDate today = LocalDate.now();
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * this.projectWorkflowService.changeProjectStatus(_, _);
    }

    def "finishProject should changeProject status to Completed"() {
        given:
            Project project = this.prepareProjectInProgressWithStatus();
            LocalDate today = LocalDate.now();
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * this.projectWorkflowService.changeProjectStatus({
                Project projectToChange ->
                    TestUtils.setFieldForObject(projectToChange, "status", ProjectStatus.COMPLETED);
            }, _);
            finishedProject.getStatus() == ProjectStatus.COMPLETED;
    }

    def "rejectProject should call changeProjectStatus on projectWorkflowService"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER);
        when:
            Project rejectedProject = this.projectDomainService.rejectProject(project);
        then:
            1 * this.projectWorkflowService.changeProjectStatus(_, _);
    }

    def "rejectProject should change project status to Rejected"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER);
        when:
            Project rejectedProject = this.projectDomainService.rejectProject(project);
        then:
            1 * this.projectWorkflowService.changeProjectStatus({
                Project projectToChange ->
                    TestUtils.setFieldForObject(projectToChange, "status", ProjectStatus.REJECTED);
            }, _);
            rejectedProject.getStatus() == ProjectStatus.REJECTED;
    }

    def "makeNewOffer should call changeProjectStatus on projectWorkflowService with status Offer"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.REJECTED);
        when:
            this.projectDomainService.makeNewOffer(project, offerDto);
        then:
            1 * this.projectWorkflowService.changeProjectStatus(_ as Project, ProjectStatus.OFFER);
    }

    def "reopenProject should call changeProjectStatus on projectWorkflowService with TODO status"() {
        given: "project with stages only in REJECTED or TODO statuses"
            Project project = this.prepareProjectWithRejectedAndToDoStages();
        when:
            this.projectDomainService.reopenProject(project);
        then:
            1 * this.projectWorkflowService.changeProjectStatus(_ as Project, ProjectStatus.TO_DO);
    }

    def "reopenProject should call changeProjectStatus on projectWorkflowService with IN_PROGRESS status"() {
        given: "project has stages in different than TODO or REJECTED statuses"
            Project project = this.prepareProjectWithDifferentStageStatuses();
        when:
            this.projectDomainService.reopenProject(project);
        then:
            1 * this.projectWorkflowService.changeProjectStatus(_ as Project, ProjectStatus.IN_PROGRESS);
    }

    // helper methods

    private void mockProjectWorkflow() {
        this.projectWorkflow.getInitialStatus() >> ProjectStatus.OFFER;
    }

    private Project prepareProjectWithStatus(ProjectStatus status) {
        return new ProjectBuilder()
                .withName(NAME)
                .withStatus(status)
                .build();
    }

    private Project prepareProjectInProgressWithStatus() {
        return new ProjectBuilder()
                .withName(NAME)
                .withStatus(ProjectStatus.IN_PROGRESS)
                .withStartDate(LocalDate.now().plusDays(20))
                .build();
    }

    private ProjectCreateDto prepareCreateProjectDto() {
        ProjectCreateDto projectCreateDto = new ProjectCreateDto();
        projectCreateDto.setName(NAME);
        projectCreateDto.setArchitectId(ARCHITECT_ID);
        projectCreateDto.setClientId(CLIENT_ID);
        projectCreateDto.setProjectType(ProjectType.CONCEPT);
        return projectCreateDto;
    }

    private ProjectDto prepareUpdateProjectDto() {
        ProjectDto projectDto = new ProjectDto();
        projectDto.setName(NEW_NAME);
        projectDto.setNote(NEW_NOTE);
        return projectDto;
    }

    private ProjectContractDto prepareProjectContractDto() {
        LocalDate now = LocalDate.now();
        ProjectContractDto projectContractDto = new ProjectContractDto();
        projectContractDto.setSigningDate(now.minusDays(10));
        projectContractDto.setStartDate(now.plusDays(10));
        projectContractDto.setDeadline(now.plusDays(50));
        return projectContractDto;
    }

    private Project prepareProjectWithRejectedAndToDoStages() {
        Set<Stage> stages = new HashSet<>();
        stages.add(prepareStageWithStatus(StageStatus.TO_DO));
        stages.add(prepareStageWithStatus(StageStatus.REJECTED));
        Project project = new ProjectBuilder().withStatus(ProjectStatus.REJECTED).withStages(stages).build();
        return project;
    }

    private Project prepareProjectWithDifferentStageStatuses() {
        Set<Stage> stages = new HashSet<>();
        stages.add(prepareStageWithStatus(StageStatus.TO_DO));
        stages.add(prepareStageWithStatus(StageStatus.REJECTED));
        stages.add(prepareStageWithStatus(StageStatus.IN_PROGRESS));
        Project project = new ProjectBuilder().withStatus(ProjectStatus.REJECTED).withStages(stages).build();
        return project;
    }

    private Stage prepareStageWithStatus(StageStatus status) {
        return new StageBuilder()
                .withName(STAGE_NAME)
                .withStatus(status)
                .build();
    }
}
