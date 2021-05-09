package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.architect.application.ArchitectValidator
import com.arturjarosz.task.architect.application.impl.ArchitectApplicationServiceImpl
import com.arturjarosz.task.client.application.ClientValidator
import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.client.application.impl.ClientApplicationServiceImpl
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.dto.ProjectContractDto
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.project.domain.impl.ProjectDomainServiceImpl
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.utils.ProjectBuilder
import spock.lang.Specification

import java.time.LocalDate

class ProjectApplicationServiceTest extends Specification {
    private static final String CLIENT_FIRST_NAME = "name";
    private static final String CLIENT_LAST_NAME = "last name";
    private static final String NEW_PROJECT_NAME = "newProjectName";
    private static final String PROJECT_NAME = "projectName";
    private static final String NEW_PROJECT_NOTE = "newProjectNote";
    private static final Long ARCHITECT_ID = 1L;
    private static final Long CLIENT_ID = 10L;
    private static final Long EXISTING_PROJECT_ID = 100L;
    private static final Long NOT_EXISTING_PROJECT_ID = 101L;
    private static final LocalDate PROJECT_DEADLINE = LocalDate.parse("2022-10-10");
    private static final LocalDate PROJECT_END_DATE = LocalDate.parse("2022-05-02");
    private static final LocalDate PROJECT_SIGNING_DATE = LocalDate.parse("2021-01-01");
    private static final LocalDate PROJECT_START_DATE = LocalDate.parse("2022-01-01");


    def clientApplicationService = Mock(ClientApplicationServiceImpl);
    def clientValidator = Mock(ClientValidator);
    def architectApplicationService = Mock(ArchitectApplicationServiceImpl);
    def architectValidator = Mock(ArchitectValidator);
    def projectRepository = Mock(ProjectRepositoryImpl);
    def projectDomainService = Mock(ProjectDomainServiceImpl);
    def projectValidator = Mock(ProjectValidator);

    def projectApplicationService = new ProjectApplicationServiceImpl(clientApplicationService, clientValidator,
            architectApplicationService, architectValidator, projectRepository, projectDomainService, projectValidator);

    def "createProject should call validateProjectBasicDto on projectValidator"() {
        given:
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto();
        when:
            ProjectDto createdProjectDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            1 * this.projectValidator.validateProjectBasicDto(_);
    }

    def "createProject should call validateArchitectExistence on architectValidator"() {
        given:
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto();
        when:
            ProjectDto createdProjectDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            1 * this.architectValidator.validateArchitectExistence(_);
    }

    def "createProject should call validateClientExistence on clientValidator"() {
        given:
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto();
        when:
            ProjectDto createdProjectDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            1 * this.clientValidator.validateClientExistence(_);
    }

    def "createProject should call createProject on projectDomainService"() {
        given:
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto();
        when:
            ProjectDto createdProjectDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            1 * this.projectDomainService.createProject(_);
    }

    def "createProject should call save on projectRepository"() {
        given:
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto();
        when:
            ProjectDto createdProjectDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            1 * this.projectRepository.save(_);
    }

    def "createProject should return newly created project"() {
        given:
            ProjectCreateDto projectCreateDto = this.prepareCreateProjectDto();
        when:
            ProjectDto createdProjectDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            1 * this.projectRepository.save(_) >> this.prepareNewProject();
            createdProjectDto.getName() == PROJECT_NAME;
    }

    def "getProject should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            ProjectDto projectDto = this.projectApplicationService.getProject(EXISTING_PROJECT_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(_);
    }

    def "getProject should return project of given id"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            ProjectDto projectDto = this.projectApplicationService.getProject(EXISTING_PROJECT_ID);
        then:
            projectDto.getId() == EXISTING_PROJECT_ID;
    }

    def "getProject should call getClientBasicData on clientApplicationService"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            ProjectDto projectDto = this.projectApplicationService.getProject(EXISTING_PROJECT_ID);
        then:
            1 * this.clientApplicationService.getClientBasicData(CLIENT_ID);
    }

    def "getProject should call getArchitect from architectApplicationService"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            ProjectDto projectDto = this.projectApplicationService.getProject(EXISTING_PROJECT_ID);
        then:
            1 * this.architectApplicationService.getArchitect(ARCHITECT_ID);
    }

    def "updateProject should call load on projectRepository"() {
        given:
            ProjectDto projectDto = this.prepareUpdateProjectDto();
        when:
            ProjectDto updatedProjectDto = this.projectApplicationService.updateProject(EXISTING_PROJECT_ID,
                    projectDto);
        then:
            1 * this.projectRepository.load(EXISTING_PROJECT_ID);
    }

    def "updateProject should call validateProjectExistence on projectValidator"() {
        given:
            ProjectDto projectDto = this.prepareUpdateProjectDto();
        when:
            ProjectDto updatedProjectDto = this.projectApplicationService.updateProject(EXISTING_PROJECT_ID,
                    projectDto);
        then:
            1 * this.projectValidator.validateProjectExistence(EXISTING_PROJECT_ID);
    }

    def "updateProject should call validateUpdateProjectDto on project validator"() {
        given:
            ProjectDto projectDto = this.prepareUpdateProjectDto();
        when:
            ProjectDto updatedProjectDto = this.projectApplicationService.updateProject(EXISTING_PROJECT_ID,
                    projectDto);
        then:
            1 * this.projectValidator.validateUpdateProjectDto(projectDto);
    }

    def "updateProject should call update on projectDomainService"() {
        given:
            ProjectDto projectDto = this.prepareUpdateProjectDto();
        when:
            ProjectDto updatedProjectDto = this.projectApplicationService.updateProject(EXISTING_PROJECT_ID,
                    projectDto);
        then:
            1 * this.projectDomainService.updateProject(_, projectDto);
    }

    def "updateProject should call save on projectRepository"() {
        given:
            ProjectDto projectDto = this.prepareUpdateProjectDto();
        when:
            ProjectDto updatedProjectDto = this.projectApplicationService.updateProject(EXISTING_PROJECT_ID,
                    projectDto);
        then:
            1 * this.projectRepository.save(_);
    }

    def "updateProject should return project with updatedData"() {
        given:
            ProjectDto projectDto = this.prepareUpdateProjectDto();
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceUpdate();
            this.mockProjectRepositorySaveUpdated();
        when:
            ProjectDto updatedProjectDto = this.projectApplicationService.updateProject(EXISTING_PROJECT_ID,
                    projectDto);
        then:
            updatedProjectDto.getId() == EXISTING_PROJECT_ID;
            updatedProjectDto.getName() == NEW_PROJECT_NAME;
    }

    def "removeProject should call validateProjectExistence on projectValidator"() {
        given:
        when:
            this.projectApplicationService.removeProject(EXISTING_PROJECT_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(EXISTING_PROJECT_ID);
    }

    def "removeProject should call remove on projectRepository"() {
        given:
        when:
            this.projectApplicationService.removeProject(EXISTING_PROJECT_ID);
        then:
            1 * this.projectRepository.remove(EXISTING_PROJECT_ID);
    }

    def "signProjectContract should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceSignProjectContract()
            ProjectContractDto projectContractDto = this.prepareProjectContractDtoForSigning();
        when:
            this.projectApplicationService.signProjectContract(EXISTING_PROJECT_ID, projectContractDto);
        then:
            1 * this.projectRepository.load(EXISTING_PROJECT_ID);
    }

    def "signProjectContract should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceSignProjectContract()
            ProjectContractDto projectContractDto = this.prepareProjectContractDtoForSigning();
        when:
            this.projectApplicationService.signProjectContract(EXISTING_PROJECT_ID, projectContractDto);
        then:
            1 * this.projectValidator.validateProjectExistence(EXISTING_PROJECT_ID);
    }

    def "singProjectContract should call validateProjectContractDto on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceSignProjectContract()
            ProjectContractDto projectContractDto = this.prepareProjectContractDtoForSigning();
        when:
            this.projectApplicationService.signProjectContract(EXISTING_PROJECT_ID, projectContractDto);
        then:
            1 * this.projectValidator.validateProjectContractDto(_);
    }

    def "signProjectContract should call signProjectContract on projectDomainService"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceSignProjectContract()
            ProjectContractDto projectContractDto = this.prepareProjectContractDtoForSigning();
        when:
            this.projectApplicationService.signProjectContract(EXISTING_PROJECT_ID, projectContractDto);
        then:
            1 * this.projectDomainService.signProjectContract(_, _);
    }

    def "signProjectContract should save signed project with projectRepository"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceSignProjectContract()
            ProjectContractDto projectContractDto = this.prepareProjectContractDtoForSigning();
        when:
            this.projectApplicationService.signProjectContract(EXISTING_PROJECT_ID, projectContractDto);
        then:
            1 * projectRepository.save({
                Project project ->
                    project.getDeadline() == PROJECT_DEADLINE;
                    project.getSigningDate() == PROJECT_SIGNING_DATE;
                    project.getStartDate() == PROJECT_START_DATE;
            });
    }

    def "finishProject should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceFinish();
            ProjectContractDto projectContractDto = this.prepareProjectContractDtoForFinish();
        when:
            this.projectApplicationService.finishProject(EXISTING_PROJECT_ID, projectContractDto);
        then:
            1 * this.projectRepository.load(EXISTING_PROJECT_ID);
    }

    def "finishProject should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceFinish();
            ProjectContractDto projectContractDto = this.prepareProjectContractDtoForFinish();
        when:
            this.projectApplicationService.finishProject(EXISTING_PROJECT_ID, projectContractDto);
        then:
            1 * this.projectValidator.validateProjectExistence(EXISTING_PROJECT_ID);
    }

    def "finishProject should call finishProject on projectDomainService"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceFinish();
            ProjectContractDto projectContractDto = this.prepareProjectContractDtoForFinish();
        when:
            this.projectApplicationService.finishProject(EXISTING_PROJECT_ID, projectContractDto);
        then:
            1 * this.projectDomainService.finishProject(_, _);
    }

    def "finishProject should save finished project with projectRepository"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceFinish();
            ProjectContractDto projectContractDto = this.prepareProjectContractDtoForFinish();
        when:
            this.projectApplicationService.finishProject(EXISTING_PROJECT_ID, projectContractDto);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.getEndDate() == PROJECT_END_DATE;
            });
    }

    def "getProjects should return list of projects"() {
        given:
            this.mockProjectRepositoryLoadAll();
        when:
            List<Project> projects = this.projectApplicationService.getProjects();
        then:
            projects.size() == 1;
    }

    def "rejectProject should call validateProjectExistence on projectValidate"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            this.projectApplicationService.rejectProject(EXISTING_PROJECT_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(EXISTING_PROJECT_ID);
    }

    def "rejectProject should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            this.projectApplicationService.rejectProject(EXISTING_PROJECT_ID);
        then:
            1 * projectRepository.load(EXISTING_PROJECT_ID);
    }

    def "rejectProject should call rejectProject on projectDomainService"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            this.projectApplicationService.rejectProject(EXISTING_PROJECT_ID);
        then:
            1 * this.projectDomainService.rejectProject(_);
    }

    def "rejectProject should save project on projectRepository"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            this.projectApplicationService.rejectProject(EXISTING_PROJECT_ID);
        then:
            1 * this.projectRepository.save(_);
    }

    def "makeNewOffer should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            this.projectApplicationService.makeNewOffer(EXISTING_PROJECT_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(EXISTING_PROJECT_ID);
    }

    def "makeNewOffer should load project from projectRepository"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            this.projectApplicationService.makeNewOffer(EXISTING_PROJECT_ID);
        then:
            1 * this.projectRepository.load(EXISTING_PROJECT_ID) >> this.prepareExistingProject();
    }

    def "makeNewOffer should call makeNewOffer from projectDomainService"() {
        given:
            this.mockProjectRepositoryLoad();
        when:
            this.projectApplicationService.makeNewOffer(EXISTING_PROJECT_ID);
        then:
            1 * this.projectDomainService.makeNewOffer(_ as Project);
    }

    def "makeNewOffer should save project with projectRepository"() {
        given:
            this.mockProjectRepositoryLoad();
            this.mockProjectDomainServiceMakeNewOffer();
        when:
            this.projectApplicationService.makeNewOffer(EXISTING_PROJECT_ID);
        then:
            1 * this.projectRepository.save(_ as Project);
    }

    private ProjectCreateDto prepareCreateProjectDto() {
        ProjectCreateDto projectCreateDto = new ProjectCreateDto();
        projectCreateDto.setName(PROJECT_NAME);
        projectCreateDto.setArchitectId(ARCHITECT_ID);
        projectCreateDto.setClientId(CLIENT_ID);
        projectCreateDto.setProjectType(ProjectType.CONCEPT);
        return projectCreateDto;
    }

    private ProjectDto prepareUpdateProjectDto() {
        ProjectDto projectDto = new ProjectDto();
        projectDto.setId(EXISTING_PROJECT_ID);
        projectDto.setName(NEW_PROJECT_NAME);
        projectDto.setNote(NEW_PROJECT_NOTE);
        return projectDto;
    }


    private Project prepareNewProject() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.OFFER)
                .build();
    }

    private Project prepareExistingProject() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withId(EXISTING_PROJECT_ID)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.OFFER)
                .build();
    }

    private Project prepareUpdatedProject() {
        return new ProjectBuilder()
                .withName(NEW_PROJECT_NAME)
                .withNote(NEW_PROJECT_NOTE)
                .withId(EXISTING_PROJECT_ID)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.OFFER)
                .build();
    }

    private Project prepareSignedProject() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withSigningDate(PROJECT_SIGNING_DATE)
                .withDeadline(PROJECT_DEADLINE)
                .withStartDate(PROJECT_START_DATE)
                .withId(EXISTING_PROJECT_ID)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.OFFER)
                .build();
    }

    private Project prepareFinishedProject() {
        return new ProjectBuilder()
                .withName(PROJECT_NAME)
                .withEndDate(PROJECT_END_DATE)
                .withId(EXISTING_PROJECT_ID)
                .withClientId(CLIENT_ID)
                .withArchitectId(ARCHITECT_ID)
                .withStatus(ProjectStatus.OFFER)
                .build();
    }

    private ClientDto prepareClientDto() {
        ClientDto clientDto = new ClientDto();
        clientDto.setFirstName(CLIENT_FIRST_NAME);
        clientDto.setLastName(CLIENT_LAST_NAME);
        return clientDto;
    }

    private ProjectContractDto prepareProjectContractDtoForSigning() {
        ProjectContractDto projectContractDto = new ProjectContractDto();
        projectContractDto.setDeadline(PROJECT_DEADLINE);
        projectContractDto.setStartDate(PROJECT_START_DATE);
        projectContractDto.setSigningDate(PROJECT_SIGNING_DATE);
        return projectContractDto;
    }

    private ProjectContractDto prepareProjectContractDtoForFinish() {
        ProjectContractDto projectContractDto = new ProjectContractDto();
        projectContractDto.setEndDate(PROJECT_END_DATE);
        return projectContractDto;
    }

    private void mockProjectRepositoryLoad() {
        this.projectRepository.load(EXISTING_PROJECT_ID) >> this.prepareExistingProject();
    }

    private void mockProjectRepositoryLoadAll() {
        1 * this.projectRepository.loadAll() >> Collections.singletonList(this.prepareExistingProject());
    }

    private void mockProjectRepositorySaveUpdated() {
        Project project = this.prepareUpdatedProject();
        this.projectRepository.save(_ as Project) >> project;
    }

    private void mockProjectDomainServiceUpdate() {
        Project project = this.prepareUpdatedProject();
        this.projectDomainService.updateProject(_ as Project, _ as ProjectDto) >> project;
    }

    private void mockProjectDomainServiceFinish() {
        Project project = this.prepareFinishedProject();
        this.projectDomainService.finishProject(_ as Project, _ as LocalDate) >> project;
    }

    private void mockProjectDomainServiceSignProjectContract() {
        Project project = this.prepareSignedProject();
        this.projectDomainService.signProjectContract(_ as Project, _ as ProjectContractDto) >> project;
    }

    private void mockProjectDomainServiceMakeNewOffer() {
        Project project = this.prepareSignedProject();
        this.projectDomainService.makeNewOffer(_ as Project) >> project;
    }
}
