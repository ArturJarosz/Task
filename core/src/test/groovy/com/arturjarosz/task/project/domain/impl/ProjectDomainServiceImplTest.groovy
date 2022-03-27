package com.arturjarosz.task.project.domain.impl

import com.arturjarosz.task.contract.application.dto.ContractDto
import com.arturjarosz.task.project.application.dto.ProjectContractDto
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.project.domain.ProjectDataValidator
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.project.status.project.impl.ProjectStatusTransitionServiceImpl
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.project.utils.StageBuilder
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import spock.lang.Ignore
import spock.lang.Specification

import java.time.LocalDate

class ProjectDomainServiceImplTest extends Specification {
    private static final String NAME = "projectName"
    private static final String NEW_NAME = "newProjectName"
    private static final String NEW_NOTE = "newNote"
    private static final String STAGE_NAME = "stageName"
    private static final Long ARCHITECT_ID = 100L
    private static final Long CLIENT_ID = 1000L
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
            Project project = this.projectDomainService.createProject(projectCreateDto, contract)
        then:
            1 * this.projectStatusTransitionService.create(_ as Project)
    }

    def "createProject should return newly created project with status OFFER"() {
        given:
            this.mockProjectWorkflow()
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto()
        when:
            Project project = this.projectDomainService.createProject(projectCreateDto, contract)
        then:
            1 * this.projectStatusTransitionService.create({
                Project createdProject ->
                    TestUtils.setFieldForObject(createdProject, "status", ProjectStatus.OFFER)
            }) >> {}
            project.name == projectCreateDto.name
            project.status == ProjectStatus.OFFER
    }

    def "updateProject should change data on project and return updated instance"() {
        given:
            ProjectDto projectDto = this.prepareUpdateProjectDto()
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER)
        when:
            Project updatedProject = this.projectDomainService.updateProject(project, projectDto)
        then:
            updatedProject.name == NEW_NAME
            updatedProject.note == NEW_NOTE
    }

    def "signProject should call signingDateNotInFuture from projectDataValidator"() {
        given:
            Project project = this.prepareProjectWithStatusAndOffer(ProjectStatus.OFFER)
            ProjectContractDto projectContractDto = this.prepareProjectContractDto()
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project, projectContractDto)
        then:
            1 * this.projectDataValidator.signingDateNotInFuture(_)
    }

    def "signProject should call startDateNotBeforeSigningDate from projectDataValidator"() {
        given:
            Project project = this.prepareProjectWithStatusAndOffer(ProjectStatus.OFFER)
            ProjectContractDto projectContractDto = this.prepareProjectContractDto()
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project, projectContractDto)
        then:
            1 * this.projectDataValidator.startDateNotBeforeSigningDate(_, _)
    }

    def "signProject should call deadlineNotBeforeStartDate from projectDataValidator"() {
        given:
            Project project = this.prepareProjectWithStatusAndOffer(ProjectStatus.OFFER)
            ProjectContractDto projectContractDto = this.prepareProjectContractDto()
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project, projectContractDto)
        then:
            1 * this.projectDataValidator.deadlineNotBeforeStartDate(_, _)
    }

    def "signProject should call acceptOffer for not accepted offer on projectStatusTransitionService"() {
        given:
            Project project = this.prepareProjectWithStatusAndOffer(ProjectStatus.OFFER)
            ProjectContractDto projectContractDto = this.prepareProjectContractDto()
            TestUtils.setFieldForObject(project.offer, "isAccepted", false)
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project,
                    projectContractDto)
        then:
            1 * this.projectStatusTransitionService.acceptOffer(_ as Project)
    }

    def "signProject should call signContract on project"() {
        given:
            Project project = Mock(Project)
            ProjectContractDto projectContractDto = this.prepareProjectContractDto()
        when:
            Project signedProject = this.projectDomainService.signProjectContract(project, projectContractDto)
        then:
            1 * project.signContract(_, _, _)
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
            LocalDate today = LocalDate.now()
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * this.projectDataValidator.endDateNotBeforeStartDate(_ as LocalDate, _ as LocalDate)
    }

    def "finishProject should call finishProject on project"() {
        given:
            Project project = Mock(Project)
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * project.finishProject(_)
    }

    @Ignore
    //TODO TA-194: analyze finishing project
    def "finishProject should call completeWork on projectStatusTransitionService"() {
        given:
            Project project = this.prepareProjectInProgressWithStatus()
            LocalDate today = LocalDate.now()
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * projectStatusTransitionService.finishWork(_ as Project)
    }

    @Ignore
    //TODO TA-194: analyze finishing project
    def "finishProject should changeProject status to Completed"() {
        given:
            Project project = this.prepareProjectInProgressWithStatus()
            LocalDate today = LocalDate.now()
        when:
            Project finishedProject = this.projectDomainService.finishProject(project, null)
        then:
            1 * projectStatusTransitionService.finishWork({
                Project projectToChange ->
                    TestUtils.setFieldForObject(projectToChange, "status", ProjectStatus.COMPLETED)
            })
            finishedProject.status == ProjectStatus.COMPLETED
    }

    def "rejectProject should call reject on projectStatusTransitionService"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER)
        when:
            Project rejectedProject = this.projectDomainService.rejectProject(project)
        then:
            1 * this.projectStatusTransitionService.reject(_ as Project)
    }

    def "rejectProject should change project status to Rejected"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER)
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

    def "makeNewOffer should not call makeNewOffer on projectStatusTransitionService for project with null status"() {
        given:
            Project project = this.prepareProjectWithStatus(null)
            ContractDto offerDto = new ContractDto()
            offerDto.offerValue = 5000
        when:
            this.projectDomainService.makeNewOffer(project, offerDto)
        then:
            0 * this.projectStatusTransitionService.makeNewOffer(_ as Project)
    }

    def "makeNewOffer should call makeNewOffer on projectStatusTransitionService for project with not null status"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER)
            ContractDto offerDto = new ContractDto()
            offerDto.setOfferValue(5000)
        when:
            this.projectDomainService.makeNewOffer(project, offerDto)
        then:
            1 * this.projectStatusTransitionService.makeNewOffer(_ as Project)
    }

    def "makeNewOffer should update project offer value"() {
        given:
            Project project = this.prepareProjectWithStatus(ProjectStatus.OFFER)
            ContractDto offerDto = new ContractDto()
            offerDto.offerValue = OFFER_VALUE
        when:
            Project updatedProject = this.projectDomainService.makeNewOffer(project, offerDto)
        then:
            updatedProject.offer.offerValue.value.doubleValue() == OFFER_VALUE
    }


    // helper methods

    private void mockProjectWorkflow() {
        this.projectWorkflow.initialStatus >> ProjectStatus.OFFER
    }

    private Project prepareProjectWithStatus(ProjectStatus status) {
        return new ProjectBuilder()
                .withName(NAME)
                .withStatus(status)
                .build()
    }

    private Project prepareProjectWithStatusAndOffer(ProjectStatus status) {
        return new ProjectBuilder()
                .withName(NAME)
                .withStatus(status)
             //   .withOffer(new Offer(5000))
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

    private ProjectContractDto prepareProjectContractDto() {
        LocalDate now = LocalDate.now()
        ProjectContractDto projectContractDto = new ProjectContractDto(signingDate: now.minusDays(10),
                startDate: now.plusDays(10), deadline: now.plusDays(50))
        return projectContractDto
    }

    private Stage prepareStageWithStatus(StageStatus status) {
        return new StageBuilder()
                .withName(STAGE_NAME)
                .withStatus(status)
                .build()
    }
}
