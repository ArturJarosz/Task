package com.arturjarosz.task.project.domain.impl


import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.dto.ProjectDto
import com.arturjarosz.task.dto.ProjectTypeDto
import com.arturjarosz.task.project.application.mapper.ProjectMapperImpl
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
    static final String NAME = "projectName"
    static final String NEW_NAME = "newProjectName"
    static final String NOTE = "note"
    static final String NEW_NOTE = "newNote"
    static final Long ARCHITECT_ID = 100L
    static final Long NEW_ARCHITECT_ID = 101L
    static final LocalDate START_DATE = LocalDate.of(2022, 01, 01)
    static final LocalDate NEW_START_DATE = LocalDate.of(2022, 01, 02)
    static final LocalDate END_DATE = LocalDate.of(2023, 01, 01)
    static final LocalDate NEW_END_DATE = LocalDate.of(2023, 01, 02)
    static final ProjectType TYPE = ProjectType.ARCHITECTURE_HOUSE
    static final ProjectType NEW_TYPE = ProjectType.CONCEPT
    static final Long CLIENT_ID = 1000L
    static final Long CONTRACT_ID = 50L

    def projectDataValidator = Mock(ProjectDataValidator)
    def projectWorkflow = Mock(ProjectWorkflow)
    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def projectMapper = new ProjectMapperImpl()

    def projectDomainService = new ProjectDomainServiceImpl(projectDataValidator, projectWorkflow,
            projectStatusTransitionService, projectMapper)

    def "createProject should call create on projectStatusTransitionService"() {
        given:
            def projectCreateDto = this.prepareCreateProjectDto()
        when:
            this.projectDomainService.createProject(projectCreateDto, CONTRACT_ID)
        then:
            1 * this.projectStatusTransitionService.create(_ as Project)
    }

    def "createProject should return newly created project with status TO_DO"() {
        given:
            this.mockProjectWorkflow()
            def projectCreateDto = this.prepareCreateProjectDto()
        when:
            def project = this.projectDomainService.createProject(projectCreateDto, CONTRACT_ID)
        then:
            1 * this.projectStatusTransitionService.create({ Project createdProject -> TestUtils.setFieldForObject(createdProject, "status", ProjectStatus.TO_DO)
            }) >> {}
            project.name == projectCreateDto.name
            project.status == ProjectStatus.TO_DO
    }

    def "updateProject should change only some data on project and return updated instance"() {
        given:
            def projectDto = new ProjectDto()
                    .name(NEW_NAME)
                    .note(NEW_NOTE)
                    .architect(new ArchitectDto().id(NEW_ARCHITECT_ID))
                    .type(ProjectTypeDto.fromValue(NEW_TYPE.name()))
                    .startDate(NEW_START_DATE)
            def project = new ProjectBuilder()
                    .withName(NAME)
                    .withNote(NOTE)
                    .withArchitectId(ARCHITECT_ID)
                    .withType(TYPE)
                    .withStartDate(START_DATE)
                    .withEndDate(END_DATE)
                    .withStatus(givenProjectStatus)
                    .build()
        when:
            def updatedProject = this.projectDomainService.updateProject(project, projectDto)
        then:
            updatedProject.name == name
            updatedProject.note == note
            updatedProject.architectId == architectId
            updatedProject.projectType == type
            updatedProject.startDate == startDate
            updatedProject.endDate == endDate
        where:
            givenProjectStatus        | name     | note     | architectId      | type     | startDate      | endDate
            ProjectStatus.TO_DO       | NEW_NAME | NEW_NOTE | NEW_ARCHITECT_ID | NEW_TYPE | START_DATE     | null
            ProjectStatus.IN_PROGRESS | NEW_NAME | NEW_NOTE | NEW_ARCHITECT_ID | NEW_TYPE | NEW_START_DATE | null
            ProjectStatus.DONE        | NEW_NAME | NEW_NOTE | NEW_ARCHITECT_ID | TYPE     | NEW_START_DATE | END_DATE
            ProjectStatus.REJECTED    | NEW_NAME | NEW_NOTE | NEW_ARCHITECT_ID | NEW_TYPE | NEW_START_DATE | null
            ProjectStatus.COMPLETED   | NEW_NAME | NEW_NOTE | NEW_ARCHITECT_ID | TYPE     | NEW_START_DATE | END_DATE
    }

    def "finishProject should set endDate to today, if endDate is not provided"() {
        given:
            def project = this.prepareProjectWithStatus(ProjectStatus.IN_PROGRESS)
            def today = LocalDate.now()
        when:
            def finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            finishedProject.endDate == today
    }

    def "finishProject should call endDateNotBeforeStartDate on projectDataValidator"() {
        given:
            def project = this.prepareProjectInProgressWithStatus()
        when:
            this.projectDomainService.finishProject(project, null)
        then:
            1 * this.projectDataValidator.endDateNotBeforeStartDate(_ as LocalDate, _ as LocalDate)
    }

    def "finishProject should call finishProject on project"() {
        given:
            def project = Mock(Project)
        when:
            this.projectDomainService.finishProject(project, null)
        then:
            1 * project.finishProject(_)
    }

    def "finishProject should call completeWork on projectStatusTransitionService"() {
        given:
            Project project = this.prepareProjectInProgressWithStatus()
        when:
            this.projectDomainService.finishProject(project, null)
        then:
            1 * projectStatusTransitionService.complete(_ as Project)
    }

    def "finishProject should changeProject status to COMPLETED"() {
        given:
            def project = this.prepareProjectInProgressWithStatus()
        when:
            def finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * projectStatusTransitionService.complete({ Project projectToChange -> TestUtils.setFieldForObject(projectToChange, "status", ProjectStatus.COMPLETED)
            })
            finishedProject.status == ProjectStatus.COMPLETED
    }

    def "rejectProject should call reject on projectStatusTransitionService"() {
        given:
            def project = this.prepareProjectWithStatus(ProjectStatus.TO_DO)
        when:
            this.projectDomainService.rejectProject(project)
        then:
            1 * this.projectStatusTransitionService.reject(_ as Project)
    }

    def "rejectProject should change project status to Rejected"() {
        given:
            def project = this.prepareProjectWithStatus(ProjectStatus.TO_DO)
        when:
            def rejectedProject = this.projectDomainService.rejectProject(project)
        then:
            1 * this.projectStatusTransitionService.reject({ Project projectToChange -> TestUtils.setFieldForObject(projectToChange, "status", ProjectStatus.REJECTED)
            })
            rejectedProject.status == ProjectStatus.REJECTED
    }

    def "reopenProject should call reopen on projectStatusTransitionService"() {
        given:
            def project = Mock(Project)
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
        def projectCreateDto = new ProjectCreateDto(name: NAME, architectId: ARCHITECT_ID,
                clientId: CLIENT_ID, type: ProjectTypeDto.CONCEPT)
        return projectCreateDto
    }

    private ProjectDto prepareUpdateProjectDto() {
        def projectDto = new ProjectDto(name: NEW_NAME, note: NEW_NOTE, architect: new ArchitectDto(id: ARCHITECT_ID), type: ProjectTypeDto.ARCHITECTURE_HOUSE)
        return projectDto
    }
}
