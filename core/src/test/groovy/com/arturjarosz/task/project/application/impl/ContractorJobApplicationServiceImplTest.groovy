package com.arturjarosz.task.project.application.impl


import com.arturjarosz.task.project.application.ContractorJobValidator
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.dto.ContractorJobDto
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.CooperatorJob
import com.arturjarosz.task.project.model.CooperatorJobType
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import spock.lang.Ignore
import spock.lang.Specification

class ContractorJobApplicationServiceImplTest extends Specification {
    private static final EXISTING_CONTRACTOR_JOB_ID = 1L;
    private static final CONTRACTOR_ID = 20L;
    private static final PROJECT_ID = 30L;
    private static final PROJECT_WITH_COOPERATOR_JOB_ID = 31L;
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
    @Ignore
    def "createContractorJob should add cooperatorJob to project"() {
        given:
            this.mockProjectRepositoryWithProjectWithoutCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDto();
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_ID, contractorJobDto);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.getCooperatorJobs().size() == 1;
            });
    }
    @Ignore
    def "deleteContractorJob should call validateProjectExistence on projectValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_COOPERATOR_JOB_ID);
    }
    @Ignore
    def "deleteContractorJob should call load on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectRepository.load(PROJECT_WITH_COOPERATOR_JOB_ID) >> this.prepareProjectWithCooperatorJob();
    }
    @Ignore
    def "deleteContractorJob should call validateContractorJobOnProjectExistence on contractorJobValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.contractorJobValidator.validateContractorJobOnProjectExistence(_, EXISTING_CONTRACTOR_JOB_ID);
    }
    @Ignore
    def "deleteContractorJob should call save on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectRepository.save(_);
    }
    @Ignore
    def "deleteContractorJob should remove existing cooperatorJob from project"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.getCooperatorJobs().size() == 0;
            });
    }
    @Ignore
    def "updateContractorJob should call validateProjectExistence on project validator"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_COOPERATOR_JOB_ID);
    }
    @Ignore
    def "updateContractorJob should call load on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.projectRepository.load(PROJECT_WITH_COOPERATOR_JOB_ID) >> this.prepareProjectWithCooperatorJob();
    }
    @Ignore
    def "updateContractorJob should call validateContractorJobOnProjectExistence on contractJobValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.contractorJobValidator.validateContractorJobOnProjectExistence(_, EXISTING_CONTRACTOR_JOB_ID);
    }
    @Ignore
    def "updateContractorJob should call validateUpdateContractorJobDto on contractorJobValidator"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
    }
    @Ignore
    def "updateContractorJob should call save on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            1 * this.projectRepository.save(_);
    }
    @Ignore
    def "updateContractorJob should update data on cooperatorJob with contractorJobId"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            ContractorJobDto contractorJobDto = this.prepareContractorJobDtoForUpdate();
        when:
            ContractorJobDto updatedContractorJobDto = this.contractorJobApplicationService
                    .updateContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID, contractorJobDto);
        then:
            updatedContractorJobDto.getName() == NEW_NAME;
            updatedContractorJobDto.getValue() == NEW_VALUE;
            updatedContractorJobDto.getNote() == NEW_NOTE;
    }
    @Ignore
    def "getContractorJob should call validateProjectExistence on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            this.mockProjectQueryService();
        when:
            ContractorJobDto contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_COOPERATOR_JOB_ID);
    }
    @Ignore
    def "getContractorJob should call load on projectRepository"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            this.mockProjectQueryService();
        when:
            ContractorJobDto contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.projectRepository.load(PROJECT_WITH_COOPERATOR_JOB_ID) >> this.prepareProjectWithCooperatorJob();
    }
    @Ignore
    def "getContractorJob should call validateContractorJobOnProjectExistence"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            this.mockProjectQueryService();
        when:
            ContractorJobDto contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID);
        then:
            1 * this.contractorJobValidator.validateContractorJobOnProjectExistence(_, EXISTING_CONTRACTOR_JOB_ID);
    }
    @Ignore
    def "getContractorJob should return contractorJobDto"() {
        given:
            this.mockProjectRepositoryWithProjectWithCooperatorJobs();
            this.mockProjectQueryService();
        when:
            ContractorJobDto contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_COOPERATOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID);
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

    private Project mockProjectRepositoryWithProjectWithoutCooperatorJobs() {
        Project project = this.prepareProjectWithoutCooperatorJobs()
        this.projectRepository.load(PROJECT_ID) >> project;
        return project;
    }

    private Project mockProjectRepositoryWithProjectWithCooperatorJobs() {
        Project project = this.prepareProjectWithCooperatorJob();
        this.projectRepository.load(PROJECT_WITH_COOPERATOR_JOB_ID) >> project;
    }

    private Project prepareProjectWithoutCooperatorJobs() {
        Project project = new Project(PROJECT_NAME, ARCHITECT_ID, CLIENT_ID, ProjectType.CONCEPT,
                new ProjectWorkflow());
        return project;
    }

    private Project prepareProjectWithCooperatorJob() {
        Project project = this.prepareProjectWithoutCooperatorJobs();
        CooperatorJob cooperatorJob = new CooperatorJob(NAME, EXISTING_CONTRACTOR_JOB_ID, CooperatorJobType
                .CONTRACTOR_JOB, VALUE, true, true);
        TestUtils.setFieldForObject(cooperatorJob, "id", EXISTING_CONTRACTOR_JOB_ID);
        project.addCooperatorJob(cooperatorJob);
        return project;
    }

    private void mockProjectQueryService() {
        CooperatorJob cooperatorJob = new CooperatorJob(NAME, EXISTING_CONTRACTOR_JOB_ID,
                CooperatorJobType.CONTRACTOR_JOB, VALUE, true, true);
        TestUtils.setFieldForObject(cooperatorJob, "id", EXISTING_CONTRACTOR_JOB_ID);
        this.projectQueryService.getCooperatorJobByIdForProject(EXISTING_CONTRACTOR_JOB_ID) >> cooperatorJob;
    }

}
