package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.architect.application.ArchitectApplicationServiceImpl
import com.arturjarosz.task.architect.application.ArchitectValidator
import com.arturjarosz.task.architect.infrastructure.repository.impl.ArchitectRepositoryImpl
import com.arturjarosz.task.architect.model.Architect
import com.arturjarosz.task.architect.utils.ArchitectBuilder
import com.arturjarosz.task.client.application.ClientValidator
import com.arturjarosz.task.client.application.impl.ClientApplicationServiceImpl
import com.arturjarosz.task.client.infrastructure.repository.impl.ClientRepositoryImpl
import com.arturjarosz.task.client.model.Client
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.dto.ProjectContractDto
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.arturjarosz.task.project.domain.impl.ProjectDomainServiceImpl
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.utils.ClientBuilder
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import spock.lang.Specification

import java.time.LocalDate

class ProjectApplicationServiceTest extends Specification {


    private static final Long NOT_EXISTING_ARCHITECT_ID = 88L;
    private static final Long NOT_EXISTING_CLIENT_ID = 99L;
    private static final Long NOT_EXISTING_PROJECT_ID = 77L;
    private static final Long EXISTING_ARCHITECT_ID = 2L;
    private static final Long EXISTING_CLIENT_ID = 1l;
    private static final Long EXISTING_PROJECT_ID = 3L;
    private static final String PROJECT_NAME = "project name";

    private static final LocalDate SIGNING_DATE = LocalDate.now();
    private static final LocalDate START_DATE = LocalDate.now().plusMonths(1);
    private static final LocalDate DEADLINE = LocalDate.now().plusMonths(2);
    private static final LocalDate END_DATE = LocalDate.now().plusMonths(1).plusDays(1);

    private static final ProjectType PROJECT_TYPE_CONCEPT = ProjectType.CONCEPT;
    private static final Architect ARCHITECT = new ArchitectBuilder().withId(EXISTING_ARCHITECT_ID).build();
    private static final Client EXISTING_CLIENT = new ClientBuilder().withId(EXISTING_CLIENT_ID).build();
    private Project project = new ProjectBuilder().withName(PROJECT_NAME).build();

    def clientApplicationService = Mock(ClientApplicationServiceImpl) {

    }
    def architectApplicationService = Mock(ArchitectApplicationServiceImpl) {

    }
    def projectRepository = Mock(ProjectRepositoryImpl) {
        load(EXISTING_PROJECT_ID) >> {
            return project;
        }
        load(NOT_EXISTING_PROJECT_ID) >> { null }
        loadAll() >> { Collections.singletonList(project) };
        save(_ as Project) >> {
            TestUtils.setFieldForObject(this.project, "id", EXISTING_PROJECT_ID);
            return this.project;
        }
    }
    def projectDomainService = Mock(ProjectDomainServiceImpl);
    def architectRepository = Mock(ArchitectRepositoryImpl) {
        load(NOT_EXISTING_ARCHITECT_ID) >> { null };
        load(EXISTING_ARCHITECT_ID) >> { this.ARCHITECT };
    }
    def projectQueryService = Mock(ProjectQueryServiceImpl) {

    }
    def architectValidator = new ArchitectValidator(architectRepository, projectQueryService);
    def clientRepository = Mock(ClientRepositoryImpl) {
        load(NOT_EXISTING_CLIENT_ID) >> { null };
        load(EXISTING_CLIENT_ID) >> { EXISTING_CLIENT }
    }
    def clientValidator = new ClientValidator(clientRepository, projectQueryService);
    def projectValidator = new ProjectValidator(projectRepository);
    def projectApplicationService = new ProjectApplicationServiceImpl(clientApplicationService, clientValidator,
            architectApplicationService, architectValidator, projectRepository, projectDomainService, projectWorkflow,
            projectValidator, projectWorkflowService);

    def "when passing not correct projectCreateDto, IllegalArgumentException should be thrown and project should not be created"() {
        given:
            ProjectCreateDto projectCreateDto = null;
        when:
            CreatedEntityDto createdEntityDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            IllegalArgumentException ex = thrown();
            createdEntityDto == null;
    }

    def "when passing not existing clientId in dto, createProject should throw an exception and project should not be created"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto();
            projectCreateDto.setArchitectId(EXISTING_ARCHITECT_ID);
            projectCreateDto.setClientId(NOT_EXISTING_CLIENT_ID);
            projectCreateDto.setProjectType(PROJECT_TYPE_CONCEPT);
            projectCreateDto.setName(PROJECT_NAME);
        when:
            CreatedEntityDto createdEntityDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            IllegalArgumentException ex = thrown();
            createdEntityDto == null;
    }

    def "when passing not existing architectId in dto, createProject should throw an exception and project should not be created"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto();
            projectCreateDto.setArchitectId(NOT_EXISTING_ARCHITECT_ID);
            projectCreateDto.setClientId(EXISTING_CLIENT_ID);
            projectCreateDto.setProjectType(PROJECT_TYPE_CONCEPT);
            projectCreateDto.setName(PROJECT_NAME);
        when:
            CreatedEntityDto createdEntityDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            IllegalArgumentException ex = thrown();
            createdEntityDto == null;
    }

    def "when passing proper dto, createProject should not throw any exception and project should be created"() {
        given:
            ProjectCreateDto projectCreateDto = new ProjectCreateDto();
            projectCreateDto.setArchitectId(EXISTING_ARCHITECT_ID);
            projectCreateDto.setClientId(EXISTING_CLIENT_ID);
            projectCreateDto.setProjectType(PROJECT_TYPE_CONCEPT);
            projectCreateDto.setName(PROJECT_NAME);
        when:
            CreatedEntityDto createdEntityDto = this.projectApplicationService.createProject(projectCreateDto);
        then:
            noExceptionThrown();
            createdEntityDto.getId() == EXISTING_PROJECT_ID;
    }

    def "when passing not existing projectId, getProject should throw an exception and project should not be loaded"() {
        given:
        when:
            ProjectDto projectDto = this.projectApplicationService.getProject(NOT_EXISTING_PROJECT_ID);
        then:
            IllegalArgumentException ex = thrown();

            projectDto == null;
    }

    def "when passing existing projectId, getProject should not throw any exception and project should be loaded"() {
        given:
        when:
            ProjectDto projectDto = this.projectApplicationService.getProject(EXISTING_PROJECT_ID);
        then:
            noExceptionThrown();
            projectDto.getName() == PROJECT_NAME;
    }

    def "when calling removeProject remove from projectRepository should be called"() {
        given:
        when:
            this.projectApplicationService.removeProject(EXISTING_PROJECT_ID);
        then:
            1 * this.projectRepository.remove(EXISTING_PROJECT_ID);
    }

    def "when calling updateProject with not existing projectId exception should be thrown and project should not be updated"() {
        given:
            ProjectDto projectDto = new ProjectDto();
        when:
            this.projectApplicationService.updateProject(NOT_EXISTING_PROJECT_ID, projectDto);
        then:
            Exception exception = thrown();
            exception.message == "notExists.project";
            0 * this.projectDomainService.updateProject(_, _);
    }

    def "when calling updateProject with null as a dto exception should be thrown and project should not be updated"() {
        given:
            ProjectDto projectDto = null;
        when:
            this.projectApplicationService.updateProject(EXISTING_PROJECT_ID, projectDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.project.update";
            0 * this.projectDomainService.updateProject(_, _);
    }

    def "when calling updateProject with null as a project name exception should be thrown and project should not be updated"() {
        given:
            ProjectDto projectDto = new ProjectDto();
        when:
            this.projectApplicationService.updateProject(EXISTING_PROJECT_ID, projectDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.project.name";
            0 * this.projectDomainService.updateProject(_, _);
    }

    def "when calling updateProject with empty project name exception should be thrown and project should not be updated"() {
        given:
            ProjectDto projectDto = new ProjectDto();
            projectDto.setName("");
        when:
            this.projectApplicationService.updateProject(EXISTING_PROJECT_ID, projectDto);
        then:
            Exception exception = thrown();
            exception.message == "isEmpty.project.name";
            0 * this.projectDomainService.updateProject(_, _);
    }

    def "when calling updateProject with proper data no exception should be thrown and project should be updated"() {
        given:
            ProjectDto projectDto = new ProjectDto();
            projectDto.setName(PROJECT_NAME);
        when:
            this.projectApplicationService.updateProject(EXISTING_PROJECT_ID, projectDto);
        then:
            noExceptionThrown();
            1 * this.projectDomainService.updateProject(_ as Project, _ as ProjectDto);
    }

    def "when calling signProjectContract not existing projectId exception should be thrown and project should not signed"() {
        given:
            ProjectContractDto projectContractDto = this.createProperProjectContractDto();
        when:
            this.projectApplicationService.signProjectContract(NOT_EXISTING_PROJECT_ID, projectContractDto);
        then:
            Exception ex = thrown();
            ex.message == "notExists.project";
            0 * this.projectDomainService.signProjectContract(_ as Project, _ as ProjectContractDto);
    }

    def "when calling signProjectContract with null as a dto exception should be thrown and project should not signed"() {
        given:
            ProjectContractDto projectContractDto = null;
        when:
            this.projectApplicationService.signProjectContract(EXISTING_PROJECT_ID, projectContractDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.contract";
            0 * this.projectDomainService.signProjectContract(_ as Project, _ as ProjectContractDto);
    }

    def "when calling singProjectContract with not proper dto exception should be thrown and project should not be signed"() {
        given:
            ProjectContractDto projectContractDto = new ProjectContractDto();
            projectContractDto.setStartDate(START_DATE);
            projectContractDto.setDeadline(DEADLINE);
        when:
            this.projectApplicationService.signProjectContract(EXISTING_PROJECT_ID, projectContractDto);
        then:
            IllegalArgumentException ex = thrown();
            0 * this.projectDomainService.signProjectContract(_ as Project, _ as ProjectContractDto);
    }

    def "when calling signProjectContract with proper data and existing projectId no exception should be thrown and contract should be signed"() {
        given:
            ProjectContractDto projectContractDto = this.createProperProjectContractDto();
        when:
            this.projectApplicationService.signProjectContract(EXISTING_PROJECT_ID, projectContractDto);
        then:
            noExceptionThrown();
            1 * this.projectDomainService.signProjectContract(_ as Project, _ as ProjectContractDto);
    }

    def "when calling finishProject with not existing projectId, exception should be thrown no project should be finished"() {
        given:
            ProjectContractDto projectContractDto = this.createProperProjectContractDto();
        when:
            this.projectApplicationService.finishProject(NOT_EXISTING_PROJECT_ID, projectContractDto);
        then:
            Exception ex = thrown();
            ex.message == "notExists.project";
            0 * this.projectDomainService.finishProject(_ as Long, _ as LocalDate);
    }

    def "when calling finishProject with existing projectId and proper dto no exception should be thrown and project should be finished"() {
        given:
            ProjectContractDto projectContractDto = new ProjectContractDto();
            projectContractDto.setEndDate(END_DATE);
        when:
            this.projectApplicationService.finishProject(EXISTING_PROJECT_ID, projectContractDto);
        then:
            noExceptionThrown()
            1 * this.projectDomainService.finishProject(_ as Long, _ as LocalDate);
    }

    def "when calling getProject list of all project should be returned"() {
        given:
        when:
            List<Project> projectList = this.projectApplicationService.getProjects();
        then:
            projectList.size() == 1;
    }

    private ProjectContractDto createProperProjectContractDto() {
        ProjectContractDto projectContractDto = new ProjectContractDto();
        projectContractDto.setSigningDate(SIGNING_DATE);
        projectContractDto.setStartDate(START_DATE);
        projectContractDto.setDeadline(DEADLINE);
        return projectContractDto;
    }

}
