package com.arturjarosz.task.project.application

import com.arturjarosz.task.cooperator.infrastructure.impl.CooperatorRepositoryImpl
import com.arturjarosz.task.cooperator.model.Cooperator
import com.arturjarosz.task.cooperator.model.CooperatorCategory
import com.arturjarosz.task.cooperator.model.CooperatorType
import com.arturjarosz.task.project.application.dto.ContractorJobDto
import com.arturjarosz.task.project.model.CooperatorJob
import com.arturjarosz.task.project.model.CooperatorJobType
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import spock.lang.Specification

class ContractorJobValidatorTest extends Specification {
    private static final String COOPERATOR_NAME = "cooperatorName";
    private static final String NAME = "name";
    private static final String NOTE = "note";
    private static final String PROJECT_NAME = "projectName"
    private static final BigDecimal VALUE = new BigDecimal(100.0);
    private static final Long ARCHITECT_ID = 99L
    private static final Long CLIENT_ID = 98L;
    private static final Long EXISTING_CONTRACTOR_ID = 1L;
    private static final Long EXISTING_CONTRACTOR_JOB_ID = 10l;
    private static final Long NOT_EXISTING_CONTRACTOR_ID = 2L;
    private static final Long NOT_EXISTING_CONTRACTOR_JOB_ID = 20L;

    def cooperatorRepository = Mock(CooperatorRepositoryImpl);
    def contractorJobValidator = new ContractorJobValidator(cooperatorRepository)


    def "when contractorJobDto is null validateCreateContractorJobDto should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = null;
        when:
            this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "isNull.contractorJob";
    }

    def "when contractorJobDto name is null validateCreateContractorJobDto should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
            contractorJobDto.setName(null);
        when:
            this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "isNull.contractorJob.name";
    }

    def "when contractorJobDto name is empty validateCreateContractorJobDto should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
            contractorJobDto.setName("");
        when:
            this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "isEmpty.contractorJob.name";
    }

    def "when contractorJobDto contractorId is null validateCreateContractor should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
            contractorJobDto.setContractorId(null);
        when:
            this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "isNull.contractorJob.contractor";
    }

    def "when contractorJobDto value is null validateCreateContractor should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
            contractorJobDto.setValue(null);
        when:
            this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "isNull.contractorJob.value";
    }

    def "when contractorJobDto value is negative validateCreatContractor should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
            contractorJobDto.setValue(-1);
        when:
            this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "negative.contractorJob.value";
    }

    def "on proper contractorJobDto validateCreatContractor should not throw any exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
        when:
            this.contractorJobValidator.validateCreateContractorJobDto(contractorJobDto);
        then:
            noExceptionThrown();
    }

    def "on not existing contractor id, validateContractorExistence should throw an exception"() {
        given:
            this.mockNotExistingCooperatorOnCooperatorRepository();
        when:
            this.contractorJobValidator.validateContractorExistence(NOT_EXISTING_CONTRACTOR_ID);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "notExist.contractor";
    }

    def "on existing contractor id, validateContractorExistence should now throw any exception"() {
        given:
            this.mockExistingCooperatorOnCooperatorRepository();
        when:
            this.contractorJobValidator.validateContractorExistence(EXISTING_CONTRACTOR_ID);
        then:
            noExceptionThrown();
    }

    def "on not existing contractorJob id, validateContractorJobExistence should throw an exception"() {
        given:
            Project project = this.prepareProjectWithContractorJob();
        when:
            this.contractorJobValidator.validateContractorJobOnProjectExistence(project,
                    NOT_EXISTING_CONTRACTOR_JOB_ID);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "notExist.project.contractorJob";
    }

    def "on contractorJob with wrong job type validateContractorJobExistence should throw an exception"() {
        given:
            Project project = this.prepareProjectWithWrongContractorJobType();
        when:
            this.contractorJobValidator.validateContractorJobOnProjectExistence(project,
                    NOT_EXISTING_CONTRACTOR_JOB_ID);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "notExist.project.contractorJob";
    }

    def "on correct contractorJobDto id validateContractorJobExistence should not throw any exception"() {
        given:
            Project project = this.prepareProjectWithContractorJob();
        when:
            this.contractorJobValidator.validateContractorJobOnProjectExistence(project, EXISTING_CONTRACTOR_JOB_ID);
        then:
            noExceptionThrown();
    }

    def "on null contractorJobDto validateUpdateContractorJobDto should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = null;
        when:
            this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "isNull.contractorJob";
    }

    def "on null contractorJobDto name validateUpdateContractorJobDto should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
            contractorJobDto.setName(null);
        when:
            this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "isNull.contractorJob.name";
    }

    def "on empty contractorJobDto name validateUpdateContractorJobDto should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
            contractorJobDto.setName("");
        when:
            this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "isEmpty.contractorJob.name";
    }

    def "when contractorJobDto value is null validateUpdateContractorJobDto should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
            contractorJobDto.setValue(null)
        when:
            this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "isNull.contractorJob.value";
    }

    def "when contractorJobDto value is negative validateUpdateContractorJobDto should throw an exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
            contractorJobDto.setValue(new BigDecimal(-1))
        when:
            this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
        then:
            IllegalArgumentException ex = thrown();
            ex.getMessage() == "negative.contractorJob.value";
    }

    def "when contractorJobDto is proper validateUpdateContractorJobDto should not throw any exception"() {
        given:
            ContractorJobDto contractorJobDto = this.prepareProperContractorJobDto();
        when:
            this.contractorJobValidator.validateUpdateContractorJobDto(contractorJobDto);
        then:
            noExceptionThrown()
    }

    private ContractorJobDto prepareProperContractorJobDto() {
        ContractorJobDto contractorJobDto = new ContractorJobDto();
        contractorJobDto.setName(NAME);
        contractorJobDto.setValue(VALUE);
        contractorJobDto.setNote(NOTE);
        contractorJobDto.setContractorId(EXISTING_CONTRACTOR_ID);
        return contractorJobDto;
    }

    private void mockNotExistingCooperatorOnCooperatorRepository() {
        this.cooperatorRepository.load(NOT_EXISTING_CONTRACTOR_ID) >> null;
    }

    private void mockExistingCooperatorOnCooperatorRepository() {
        Cooperator cooperator = new Cooperator(COOPERATOR_NAME, CooperatorType.CONTRACTOR, CooperatorCategory
                .ContractorCategory.CARPENTER.asCooperatorCategory());
        this.cooperatorRepository.load(EXISTING_CONTRACTOR_ID) >> cooperator;
    }

    private Project prepareProjectWithContractorJob() {
        CooperatorJob cooperatorJob = new CooperatorJob(NAME, EXISTING_CONTRACTOR_ID, CooperatorJobType.CONTRACTOR_JOB,
                VALUE, true, true);
        TestUtils.setFieldForObject(cooperatorJob, "id", EXISTING_CONTRACTOR_JOB_ID);
        Project project = this.prepareProjectWithoutCooperatorJob();
        project.addCooperatorJob(cooperatorJob);
        return project;
    }

    private Project prepareProjectWithWrongContractorJobType() {
        CooperatorJob cooperatorJob = new CooperatorJob(NAME, EXISTING_CONTRACTOR_ID, CooperatorJobType.SUPPLY, VALUE,
                true, true);
        TestUtils.setFieldForObject(cooperatorJob, "id", EXISTING_CONTRACTOR_JOB_ID);
        Project project = this.prepareProjectWithoutCooperatorJob();
        project.addCooperatorJob(cooperatorJob);
        return project;
    }

    private Project prepareProjectWithoutCooperatorJob() {
        Project project = new Project(PROJECT_NAME, ARCHITECT_ID, CLIENT_ID, ProjectType.CONCEPT,
                new ProjectWorkflow());
        return project;
    }
}
