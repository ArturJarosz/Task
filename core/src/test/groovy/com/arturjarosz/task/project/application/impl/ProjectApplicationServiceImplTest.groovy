package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.architect.application.ArchitectValidator
import com.arturjarosz.task.architect.application.impl.ArchitectApplicationServiceImpl
import com.arturjarosz.task.client.application.ClientValidator
import com.arturjarosz.task.client.application.impl.ClientApplicationServiceImpl
import com.arturjarosz.task.contract.application.impl.ContractServiceImpl
import com.arturjarosz.task.contract.application.mapper.ContractMapperImpl
import com.arturjarosz.task.contract.model.Contract
import com.arturjarosz.task.contract.status.ContractStatusWorkflow
import com.arturjarosz.task.dto.*
import com.arturjarosz.task.finance.application.CostApplicationService
import com.arturjarosz.task.finance.application.ProjectFinancialDataService
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.mapper.ProjectMapperImpl
import com.arturjarosz.task.project.domain.impl.ProjectDomainServiceImpl
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

import java.time.LocalDate

class ProjectApplicationServiceImplTest extends Specification {
    static final String NEW_PROJECT_NAME = "newProjectName"
    static final String PROJECT_NAME = "projectName"
    static final String NEW_PROJECT_NOTE = "newProjectNote"
    static final Long ARCHITECT_ID = 1L
    static final Long CLIENT_ID = 10L
    static final Long EXISTING_PROJECT_ID = 100L
    static final Long NEW_PROJECT_ID = 101L
    static final Long CONTRACT_ID = 1000L
    static final LocalDate PROJECT_DEADLINE = LocalDate.parse("2022-10-10")
    static final LocalDate PROJECT_END_DATE = LocalDate.parse("2022-05-02")
    static final LocalDate PROJECT_START_DATE = LocalDate.parse("2022-01-01")
    static final Double PROJECT_VALUE = 5000.0

    def clientApplicationService = Mock(ClientApplicationServiceImpl)
    def clientValidator = Mock(ClientValidator)
    def architectApplicationService = Mock(ArchitectApplicationServiceImpl)
    def architectValidator = Mock(ArchitectValidator)
    def projectRepository = Mock(ProjectRepository)
    def projectDomainService = Mock(ProjectDomainServiceImpl)
    def projectValidator = Mock(ProjectValidator)
    def contractService = Mock(ContractServiceImpl)
    def contractWorkflow = Mock(ContractStatusWorkflow)
    def projectFinancialDataService = Mock(ProjectFinancialDataService)
    def contractMapper = new ContractMapperImpl()
    def projectMapper = new ProjectMapperImpl()
    def costApplicationService = Mock(CostApplicationService)

    def projectApplicationService = new ProjectApplicationServiceImpl(clientApplicationService, clientValidator,
            architectApplicationService, architectValidator, projectRepository, projectDomainService, projectValidator,
            projectFinancialDataService, contractService, contractMapper, projectMapper, costApplicationService)

    def "createProject should call validateProjectBasicDto on projectValidator"() {
        given:
            this.mockProjectRepositorySaveNewProject()
            this.mockContractServiceCreateContract()
            def projectCreateDto = this.prepareCreateProjectDto()

        when:
            this.projectApplicationService.createProject(projectCreateDto)

        then:
            1 * this.projectValidator.validateProjectBasicDto(_)
    }

    def "createProject should call validateArchitectExistence on architectValidator"() {
        given:
            this.mockProjectRepositorySaveNewProject()
            this.mockContractServiceCreateContract()
            def projectCreateDto = this.prepareCreateProjectDto()
        when:
            this.projectApplicationService.createProject(projectCreateDto)
        then:
            1 * this.architectValidator.validateArchitectExistence(_)
    }

    def "createProject should call validateClientExistence on clientValidator"() {
        given:
            this.mockProjectRepositorySaveNewProject()
            this.mockContractServiceCreateContract()
            def projectCreateDto = this.prepareCreateProjectDto()
        when:
            this.projectApplicationService.createProject(projectCreateDto)
        then:
            1 * this.clientValidator.validateClientExistence(_)
    }

    def "createProject should call createProject on projectDomainService"() {
        given:
            this.mockProjectRepositorySaveNewProject()
            this.mockContractServiceCreateContract()
            def projectCreateDto = this.prepareCreateProjectDto()
        when:
            this.projectApplicationService.createProject(projectCreateDto)
        then:
            1 * this.projectDomainService.createProject(_, CONTRACT_ID)
    }

    def "createProject should call save on projectRepository"() {
        given:
            this.mockContractServiceCreateContract()
            def projectCreateDto = this.prepareCreateProjectDto()
        when:
            this.projectApplicationService.createProject(projectCreateDto)
        then:
            1 * this.projectRepository.save(_) >> this.prepareNewlyCreatedProject()
    }

    def "createProject should call createProjectFinancialDataOnProjectFinancialDataApplicationService"() {
        given:
            this.mockProjectRepositorySaveNewProject()
            this.mockContractServiceCreateContract()
            def projectCreateDto = this.prepareCreateProjectDto()
        when:
            this.projectApplicationService.createProject(projectCreateDto)
        then:
            1 * this.projectFinancialDataService.createProjectFinancialData(NEW_PROJECT_ID)
    }

    def "createProject should return newly created project"() {
        given:
            this.mockContractServiceCreateContract()
            def projectCreateDto = this.prepareCreateProjectDto()
        when:
            def createdProjectDto = this.projectApplicationService.createProject(projectCreateDto)
        then:
            1 * this.projectRepository.save(_) >> this.prepareNewProject()
            createdProjectDto.name == PROJECT_NAME
    }

    def "getProject should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.getProject(EXISTING_PROJECT_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, EXISTING_PROJECT_ID)
    }

    def "getProject should return project of given id"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            def projectDto = this.projectApplicationService.getProject(EXISTING_PROJECT_ID)
        then:
            projectDto.id == EXISTING_PROJECT_ID
    }

    def "getProject should call getClientBasicData on clientApplicationService"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.getProject(EXISTING_PROJECT_ID)
        then:
            1 * this.clientApplicationService.getClientBasicData(CLIENT_ID)
    }

    def "getProject should call getArchitect from architectApplicationService"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.getProject(EXISTING_PROJECT_ID)
        then:
            1 * this.architectApplicationService.getArchitect(ARCHITECT_ID)
    }

    def "updateProject should call load on projectRepository"() {
        given:
            def projectDto = this.prepareUpdateProjectDto()
        when:
            this.projectApplicationService.updateProject(EXISTING_PROJECT_ID, projectDto)
        then:
            1 * this.projectRepository.findById(EXISTING_PROJECT_ID) >> Optional.of(this.prepareExistingProject())
    }

    def "updateProject should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad()
            def projectDto = this.prepareUpdateProjectDto()
        when:
            this.projectApplicationService.updateProject(EXISTING_PROJECT_ID, projectDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, EXISTING_PROJECT_ID)
    }

    def "updateProject should call validateUpdateProjectDto on project validator"() {
        given:
            this.mockProjectRepositoryLoad()
            def projectDto = this.prepareUpdateProjectDto()
        when:
            this.projectApplicationService.updateProject(EXISTING_PROJECT_ID, projectDto)
        then:
            1 * this.projectValidator.validateUpdateProjectDto(projectDto)
    }

    def "updateProject should call update on projectDomainService"() {
        given:
            this.mockProjectRepositoryLoad()
            def projectDto = this.prepareUpdateProjectDto()
        when:
            this.projectApplicationService.updateProject(EXISTING_PROJECT_ID, projectDto)
        then:
            1 * this.projectDomainService.updateProject(_, projectDto)
    }

    def "updateProject should call save on projectRepository"() {
        given:
            this.mockProjectRepositoryLoad()
            def projectDto = this.prepareUpdateProjectDto()
        when:
            this.projectApplicationService.updateProject(EXISTING_PROJECT_ID, projectDto)
        then:
            1 * this.projectRepository.save(_)
    }

    def "updateProject should return project with updatedData"() {
        given:
            def projectDto = this.prepareUpdateProjectDto()
            this.mockProjectRepositoryLoad()
            this.mockProjectDomainServiceUpdate()
            this.mockProjectRepositorySaveUpdated()
        when:
            def updatedProjectDto = this.projectApplicationService.updateProject(EXISTING_PROJECT_ID,
                    projectDto)
        then:
            updatedProjectDto.id == EXISTING_PROJECT_ID
            updatedProjectDto.name == NEW_PROJECT_NAME
    }

    def "removeProject should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.removeProject(EXISTING_PROJECT_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(EXISTING_PROJECT_ID)
    }

    def "removeProject should call remove on projectRepository"() {
        given:
        when:
            this.projectApplicationService.removeProject(EXISTING_PROJECT_ID)
        then:
            1 * this.projectRepository.deleteById(EXISTING_PROJECT_ID)
    }

    def "finishProject should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoad()
            this.mockProjectDomainServiceFinish()
            def projectContractDto = this.prepareProjectDtoForFinish()
        when:
            this.projectApplicationService.finishProject(EXISTING_PROJECT_ID, projectContractDto)
        then:
            1 * this.projectRepository.findById(EXISTING_PROJECT_ID) >> Optional.of(this.prepareExistingProject())
    }

    def "finishProject should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad()
            this.mockProjectDomainServiceFinish()
            def projectContractDto = this.prepareProjectDtoForFinish()
        when:
            this.projectApplicationService.finishProject(EXISTING_PROJECT_ID, projectContractDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, EXISTING_PROJECT_ID)
    }

    def "finishProject should call finishProject on projectDomainService"() {
        given:
            this.mockProjectRepositoryLoad()
            this.mockProjectDomainServiceFinish()
            def projectContractDto = this.prepareProjectDtoForFinish()
        when:
            this.projectApplicationService.finishProject(EXISTING_PROJECT_ID, projectContractDto)
        then:
            1 * this.projectDomainService.finishProject(_, _)
    }

    def "finishProject should save finished project with projectRepository"() {
        given:
            this.mockProjectRepositoryLoad()
            this.mockProjectDomainServiceFinish()
            def projectContractDto = this.prepareProjectDtoForFinish()
        when:
            this.projectApplicationService.finishProject(EXISTING_PROJECT_ID, projectContractDto)
        then:
            1 * this.projectRepository.save({ Project project -> project.endDate == PROJECT_END_DATE
            })
    }

    def "getProjects should return list of projects"() {
        given:
            this.mockProjectRepositoryLoadAll()
        when:
            List<ProjectDto> projects = this.projectApplicationService.getProjects()
        then:
            projects.size() == 1
    }

    def "rejectProject should call validateProjectExistence on projectValidate"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.rejectProject(EXISTING_PROJECT_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, EXISTING_PROJECT_ID)
    }

    def "rejectProject should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.rejectProject(EXISTING_PROJECT_ID)
        then:
            1 * projectRepository.findById(EXISTING_PROJECT_ID) >> Optional.of(this.prepareExistingProject())
    }

    def "rejectProject should call rejectProject on projectDomainService"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.rejectProject(EXISTING_PROJECT_ID)
        then:
            1 * this.projectDomainService.rejectProject(_)
    }

    def "rejectProject should save project on projectRepository"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.rejectProject(EXISTING_PROJECT_ID)
        then:
            1 * this.projectRepository.save(_)
    }

    def "reopenProject should call validateProjectExistence on projectValidate"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.reopenProject(EXISTING_PROJECT_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, EXISTING_PROJECT_ID)
    }

    def "reopenProject should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.reopenProject(EXISTING_PROJECT_ID)
        then:
            1 * projectRepository.findById(EXISTING_PROJECT_ID) >> Optional.of(this.prepareExistingProject())
    }

    def "reopenProject should call reopenProject on projectDomainService"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.reopenProject(EXISTING_PROJECT_ID)
        then:
            1 * this.projectDomainService.reopenProject(_)
    }

    def "reopenProject should save project on projectRepository"() {
        given:
            this.mockProjectRepositoryLoad()
        when:
            this.projectApplicationService.reopenProject(EXISTING_PROJECT_ID)
        then:
            1 * this.projectRepository.save(_)
    }

    private ProjectCreateDto prepareCreateProjectDto() {
        def projectCreateDto = new ProjectCreateDto(name: PROJECT_NAME, architectId: ARCHITECT_ID,
                clientId: CLIENT_ID, type: ProjectTypeDto.CONCEPT, offerValue: PROJECT_VALUE)
        return projectCreateDto
    }

    private ProjectDto prepareUpdateProjectDto() {
        def projectDto = new ProjectDto(id: EXISTING_PROJECT_ID, name: NEW_PROJECT_NAME, note: NEW_PROJECT_NOTE)
        return projectDto
    }


    private Project prepareNewProject() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.TO_DO)
                .build()
    }

    private Project prepareExistingProject() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withId(EXISTING_PROJECT_ID)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.TO_DO)
                .build()
    }

    private Project prepareUpdatedProject() {
        return new ProjectBuilder()
                .withName(NEW_PROJECT_NAME)
                .withNote(NEW_PROJECT_NOTE)
                .withId(EXISTING_PROJECT_ID)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.TO_DO)
                .build()
    }

    private Project prepareSignedProject() {
        Contract contract = new Contract(PROJECT_VALUE, PROJECT_DEADLINE, contractWorkflow)
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withContract(contract)
                .withStartDate(PROJECT_START_DATE)
                .withId(EXISTING_PROJECT_ID)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.TO_DO)
                .withContract(contract)
                .build()
    }

    private Project prepareFinishedProject() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withEndDate(PROJECT_END_DATE)
                .withId(EXISTING_PROJECT_ID)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.TO_DO)
                .build()
    }

    private ProjectDto prepareProjectDtoForFinish() {
        return new ProjectDto(endDate: PROJECT_END_DATE)
    }

    private void mockProjectRepositoryLoad() {
        this.projectRepository.findById(EXISTING_PROJECT_ID) >> Optional.of(this.prepareExistingProject())
    }

    private void mockProjectRepositoryLoadAll() {
        1 * this.projectRepository.findAll() >> Collections.singletonList(this.prepareExistingProject())
    }

    private void mockProjectRepositorySaveUpdated() {
        def project = this.prepareUpdatedProject()
        this.projectRepository.save(_ as Project) >> project
    }

    private void mockProjectDomainServiceUpdate() {
        def project = this.prepareUpdatedProject()
        this.projectDomainService.updateProject(_ as Project, _ as ProjectDto) >> project
    }

    private void mockProjectDomainServiceFinish() {
        def project = this.prepareFinishedProject()
        this.projectDomainService.finishProject(_ as Project, _ as LocalDate) >> project
    }

    private void mockProjectRepositorySaveNewProject() {
        1 * this.projectRepository.save(_) >> this.prepareNewlyCreatedProject()
    }

    private Project prepareNewlyCreatedProject() {
        return new ProjectBuilder().withId(NEW_PROJECT_ID).withStatus(ProjectStatus.TO_DO)
                .build()
    }

    private void mockContractServiceCreateContract() {
        this.contractService.createContract(_ as ContractDto) >> new ContractDto()
                .id(CONTRACT_ID)
                .status(ContractStatusDto.OFFER)
                .offerValue(PROJECT_VALUE)
    }
}
