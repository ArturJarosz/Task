package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.project.application.ContractorJobValidator
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.dto.ContractorJobDto
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.ContractorJob
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import spock.lang.Specification

class ContractorJobApplicationServiceImplTest extends Specification {
    private static final EXISTING_CONTRACTOR_JOB_ID = 1L;
    private static final CONTRACTOR_ID = 20L;
    private static final PROJECT_ID = 30L;
    private static final PROJECT_WITH_CONTRACTOR_JOB_ID = 31L;
    private static final Long ARCHITECT_ID = 40L;
    private static final Long CLIENT_ID = 41L;
    private static final BigDecimal VALUE = new BigDecimal(100.0);
    private static final BigDecimal NEW_VALUE = new BigDecimal(200.0);
    private static final String NAME = "name";
    private static final String NEW_NAME = "newName";
    private static final String NOTE = "note";
    private static final String NEW_NOTE = "newNote";
    private static final String PROJECT_NAME = "project name";

    def contractorJobValidator = Mock(ContractorJobValidator);
    def projectQueryService = Mock(ProjectQueryServiceImpl);
    def projectRepository = Mock(ProjectRepositoryImpl);
    def projectValidator = Mock(ProjectValidator);

    def contractorJobApplicationService = new ContractorJobApplicationServiceImpl(contractorJobValidator,
            projectQueryService, projectRepository, projectValidator);

    def "createContractorJob should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithoutCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDto();
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_ID, contractorJobDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "createContractorJob should call validateCreateContractorJobDto on projectValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithoutCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDto();
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_ID, contractorJobDto);
        then:
            1 * this.contractorJobValidator.validateCreateContractorJobDto(_);
    }

    def "createContractorJob should call validateContractorExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithoutCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDto();
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_ID, contractorJobDto);
        then:
            1 * this.contractorJobValidator.validateContractorExistence(_);
    }

    def "createContractorJob should call load on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithoutCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDto();
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_ID, contractorJobDto);
        then:
            1 * this.projectRepository.load(PROJECT_ID) >> this.prepareProjectWithoutCooperatorJobs();
    }

    def "createContractorJob should call save on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithoutCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDto();
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_ID, contractorJobDto);
        then:
            1 * projectRepository.save(_);
    }

    def "createContractorJob should add cooperatorJob to project"() {
        given:
            this.mockProjectRepositoryWithProjectWithoutCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDto();
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_ID, contractorJobDto);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.getContractorJobs().size() == 1;
            });
    }

    def "deleteContractorJob should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_CONTRACTOR_JOB_ID);
    }

    def "deleteContractorJob should call load on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectRepository.load(PROJECT_WITH_CONTRACTOR_JOB_ID) >> this.prepareProjectWithContractorJob();
    }

    def "deleteContractorJob should call validateContractorJobOnProjectExistence on contractorJobValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.contractorJobValidator.validateContractorJobOnProjectExistence(_, EXISTING_CONTRACTOR_JOB_ID);
    }

    def "deleteContractorJob should call save on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectRepository.save(_);
    }

    def "deleteContractorJob should remove existing cooperatorJob from project"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.getContractorJobs().size() == 0;
            });
    }

    def "updateContractorJob should call validateProjectExistence on project validator"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_CONTRACTOR_JOB_ID);
    }

    def "updateContractorJob should call load on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.projectRepository.load(PROJECT_WITH_CONTRACTOR_JOB_ID) >> this.prepareProjectWithContractorJob();
    }

    def "updateContractorJob should call validateContractorJobOnProjectExistence on contractJobValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.contractorJobValidator.validateContractorJobOnProjectExistence(_, EXISTING_CONTRACTOR_JOB_ID);
    }

    def "updateContractorJob should call validateUpdateContractorJobDto on contractorJobValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
    }

    def "updateContractorJob should call save on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.projectRepository.save(_);
    }

    def "updateContractorJob should update data on cooperatorJob with contractorJobId"() {
        given:
            this.mockProjectRepositoryWithProjectWithContractorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            ContractorJobDto updatedContractorJobDto = this.contractorJobApplicationService
                    .updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            updatedContractorJobDto.getName() == NEW_NAME;
            updatedContractorJobDto.getValue() == NEW_VALUE;
            updatedContractorJobDto.getNote() == NEW_NOTE;
    }

    def "getContractorJob should call validateProjectExistence on projectRepository"() {
        given:
            this.mockProjectQueryService();
        when:
            ContractorJobDto contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_CONTRACTOR_JOB_ID);
    }

    def "getContractorJob should call getContractorJobForProject on projectQueryService"() {
        given:
            this.mockProjectQueryService();
        when:
            ContractorJobDto contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectQueryService.getContractorJobForProject(EXISTING_CONTRACTOR_JOB_ID,
                    PROJECT_WITH_CONTRACTOR_JOB_ID) >> this.prepareContractorJobDto();
    }

    def "getContractorJob should call validateContractorJobOnProjectExistence"() {
        given:
            this.mockProjectQueryService();
        when:
            ContractorJobDto contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.contractorJobValidator.validateContractorJobOnProjectExistence(_, EXISTING_CONTRACTOR_JOB_ID);
    }

    def "getContractorJob should return contractorJobDto"() {
        given:
            this.mockProjectQueryService();
        when:
            ContractorJobDto contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID);
        then:
            contractorJobDto != null;
    }

    private ContractorJobDto prepareContractorJobDto() {
        ContractorJobDto contractorJobDto = new ContractorJobDto();
        contractorJobDto.setName(NAME);
        contractorJobDto.setNote(NOTE);
        contractorJobDto.setValue(VALUE);
        contractorJobDto.setContractorId(CONTRACTOR_ID);
        contractorJobDto.setId(EXISTING_CONTRACTOR_JOB_ID);
        contractorJobDto.setHasInvoice(true);
        contractorJobDto.setPayable(true);
        return contractorJobDto;
    }

    private ContractorJobDto prepareContractorJobDtoForUpdate() {
        ContractorJobDto contractorJobDto = new ContractorJobDto();
        contractorJobDto.setName(NEW_NAME);
        contractorJobDto.setNote(NEW_NOTE);
        contractorJobDto.setValue(NEW_VALUE);
        contractorJobDto.setContractorId(CONTRACTOR_ID);
        contractorJobDto.setId(EXISTING_CONTRACTOR_JOB_ID);
        contractorJobDto.setHasInvoice(true);
        contractorJobDto.setPayable(true);
        return contractorJobDto;
    }

    private void mockProjectRepositoryWithProjectWithoutCooperatorJobs() {
        Project project = this.prepareProjectWithoutCooperatorJobs()
        this.projectRepository.load(PROJECT_ID) >> project;
    }

    private void mockProjectRepositoryWithProjectWithContractorJobs() {
        Project project = this.prepareProjectWithContractorJob();
        this.projectRepository.load(PROJECT_WITH_CONTRACTOR_JOB_ID) >> project;
    }

    private Project prepareProjectWithoutCooperatorJobs() {
        Project project = new Project(PROJECT_NAME, ARCHITECT_ID, CLIENT_ID, ProjectType.CONCEPT,
                new ProjectWorkflow());
        return project;
    }

    private Project prepareProjectWithContractorJob() {
        Project project = this.prepareProjectWithoutCooperatorJobs();
        ContractorJob contractorJob = new ContractorJob(NAME, EXISTING_CONTRACTOR_JOB_ID, VALUE, true, true);
        TestUtils.setFieldForObject(contractorJob, "id", EXISTING_CONTRACTOR_JOB_ID);
        project.addContractorJob(contractorJob);
        return project;
    }

    private void mockProjectQueryService() {
        ContractorJobDto contractorJobDto = this.prepareContractorJobDto();
        contractorJobDto.setId(EXISTING_CONTRACTOR_JOB_ID);
        this.projectQueryService.getContractorJobForProject(EXISTING_CONTRACTOR_JOB_ID,
                PROJECT_WITH_CONTRACTOR_JOB_ID) >> contractorJobDto ;
    }

}
