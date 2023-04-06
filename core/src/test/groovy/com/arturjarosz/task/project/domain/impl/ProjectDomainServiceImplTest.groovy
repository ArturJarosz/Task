package com.arturjarosz.task.project.domain.impl

import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.project.domain.ProjectDataValidator
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.project.status.project.impl.ProjectStatusTransitionServiceImpl
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

import java.time.LocalDate

class ProjectDomainServiceImplTest extends Specification {
    private static final String NAME = "projectName"
    private static final String NEW_NAME = "newProjectName"
    private static final String NEW_NOTE = "newNote"
    private static final String STAGE_NAME = "stageName"
    private static final Long ARCHITECT_ID = 100L
    private static final Long CLIENT_ID = 1000L
    private static final Long CONTRACT_ID = 50L
    private static final double OFFER_VALUE = 5000.0

    def projectDataValidator = Mock(ProjectDataValidator)
    def projectWorkflow = Mock(ProjectWorkflow)
    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)

    def projectDomainService = new ProjectDomainServiceImpl(projectDataValidator, projectWorkflow,
            projectStatusTransitionService)

    def "createProject should call create on projectStatusTransitionService"() {
        given:
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto()
        when:
            this.projectDomainService.createProject(projectCreateDto, CONTRACT_ID)
        then:
            1 * this.projectStatusTransitionService.create(_ as Project)
    }

    def "createProject should return newly created project with status TO_DO"() {
        given:
            this.mockProjectWorkflow()
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto()
        when:
            Project project = this.projectDomainService.createProject(projectCreateDto, CONTRACT_ID)
        then:
            1 * this.projectStatusTransitionService.create({
                Project createdProject ->
                    TestUtils.setFieldForObject(createdProject, "status", ProjectStatus.TO_DO)
            }) >> {}
            project.name == projectCreateDto.name
            project.status == ProjectStatus.TO_DO
    }

    def "updateProject should change data on project and return updated instance"() {
        given:
            ProjectDto projectDto = this.prepareUpdateProjectDto()
            Project project = this.prepareProjectWithStatus(ProjectStatus.TO_DO)
        when:
            Project updatedProject = this.projectDomainService.updateProject(project, projectDto)
        then:
            updatedProject.name == NEW_NAME
            updatedProject.note == NEW_NOTE
    }

    def "finishProject should set endDate to today, if endDate is not provided"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.IN_PROGRESS)
            LocalDate today = LocalDate.now()
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            finishedProject.endDate == today
    }

    def "finishProject should call endDateNotBeforeStartDate on projectDataValidator"() {
        given:
            Project project = this.prepareProjectInProgressWithStatus()
        when:
            this.projectDomainService.finishProject(project, null)
        then:
            1 * this.projectDataValidator.endDateNotBeforeStartDate(_ as LocalDate, _ as LocalDate)
    }

    def "finishProject should call finishProject on project"() {
        given:
            Project project = Mock(Project)
        when:
            this.projectDomainService.finishProject(project, null)
        then:
            1 * project.finishProject(_)
    }

    //TODO TA-194: analyze finishing project
    def "finishProject should call completeWork on projectStatusTransitionService"() {
        given:
            Project project = this.prepareProjectInProgressWithStatus()
        when:
            this.projectDomainService.finishProject(project, null)
        then:
            1 * projectStatusTransitionService.complete(_ as Project)
    }

    //TODO TA-194: analyze finishing project
    def "finishProject should changeProject status to COMPLETED"() {
        given:
            Project project = this.prepareProjectInProgressWithStatus()
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * projectStatusTransitionService.complete({
                Project projectToChange ->
                    TestUtils.setFieldForObject(projectToChange, "status", ProjectStatus.COMPLETED)
            })
            finishedProject.status == ProjectStatus.COMPLETED
    }

    def "rejectProject should call reject on projectStatusTransitionService"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.TO_DO)
        when:
            this.projectDomainService.rejectProject(project)
        then:
            1 * this.projectStatusTransitionService.reject(_ as Project)
    }

    def "rejectProject should change project status to Rejected"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.TO_DO)
        when:
            Project rejectedProject = this.projectDomainService.rejectProject(project)
        then:
            1 * this.projectStatusTransitionService.reject({
                Project projectToChange ->
                    TestUtils.setFieldForObject(projectToChange, "status", ProjectStatus.REJECTED)
            })
            rejectedProject.status == ProjectStatus.REJECTED
    }

    def "reopenProject should call reopen on projectStatusTransitionService"() {
        given:
            Project project = Mock(Project)
        when:
            this.projectDomainService.reopenProject(project)
        then:
            1 * this.projectStatusTransitionService.reopen(_ as Project)
    }

    // helper methods

    private void mockProjectWorkflow() {
        this.projectWorkflow.initialStatus >> ProjectStatus.TO_DO
    }

    private Project prepareProjectWithStatus(ProjectStatus status) {
        return new ProjectBuilder()
                .withName(NAME)
                .withStatus(status)
                .build()
    }

    private Project prepareProjectInProgressWithStatus() {
        return new ProjectBuilder()
                .withName(NAME)
                .withStatus(ProjectStatus.IN_PROGRESS)
                .withStartDate(LocalDate.now().plusDays(20))
                .build()
    }

    private ProjectCreateDto prepareCreateProjectDto() {
        ProjectCreateDto projectCreateDto = new ProjectCreateDto(name: NAME, architectId: ARCHITECT_ID,
                clientId: CLIENT_ID, projectType: ProjectType.CONCEPT)
        return projectCreateDto
    }

    private ProjectDto prepareUpdateProjectDto() {
        ProjectDto projectDto = new ProjectDto(name: NEW_NAME, note: NEW_NOTE)
        return projectDto
    }
}
