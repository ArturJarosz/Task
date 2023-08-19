package com.arturjarosz.task.finance.application.impl

import com.arturjarosz.task.dto.ContractorJobDto
import com.arturjarosz.task.finance.application.validator.ContractorJobValidator
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository
import com.arturjarosz.task.finance.model.ContractorJob
import com.arturjarosz.task.finance.model.ProjectFinancialData
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

class ContractorJobApplicationServiceImplTest extends Specification {
    static final EXISTING_CONTRACTOR_JOB_ID = 1L
    static final NOT_EXISTING_CONTRACTOR_JOB_ID = 2L
    static final CONTRACTOR_ID = 20L
    static final NOT_EXISING_CONTRACTOR_ID = 21L
    static final PROJECT_WITHOUT_CONTRACTOR_JOB_ID = 30L
    static final PROJECT_WITH_CONTRACTOR_JOB_ID = 31L
    static final NOT_EXISTING_PROJECT_ID = 32L
    static final BigDecimal VALUE = new BigDecimal("100.0")
    static final BigDecimal NEW_VALUE = new BigDecimal("200.0")
    static final String NAME = "name"
    static final String NEW_NAME = "newName"
    static final String NOTE = "note"
    static final String NEW_NOTE = "newNote"

    def contractorJobValidator = Mock(ContractorJobValidator)
    def projectFinanceAwareObjectService = Mock(ProjectFinanceAwareObjectServiceImpl)
    def projectValidator = Mock(ProjectValidator)
    def projectFinancialDataRepository = Mock(ProjectFinancialDataRepository)
    def financialDataQueryService = Mock(FinancialDataQueryService)

    def contractorJobApplicationService = new ContractorJobApplicationServiceImpl(contractorJobValidator,
            projectFinanceAwareObjectService, projectValidator, projectFinancialDataRepository,
            financialDataQueryService)

    def setup() {
        projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_WITH_CONTRACTOR_JOB_ID) >>
                prepareProjectFinancialDataWithContactorJob()
        projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_WITHOUT_CONTRACTOR_JOB_ID) >>
                prepareProjectFinancialDataWithoutContractorJob()
        financialDataQueryService.getContractorJobById(EXISTING_CONTRACTOR_JOB_ID) >> new ContractorJobDto()
        projectValidator.validateProjectExistence(NOT_EXISTING_PROJECT_ID) >> { throw new IllegalArgumentException() }
    }


    def "createContractorJob should not create contractorJob if project validation fails"() {
        given:
            def contractorJobDto = this.prepareContractorJobDto(CONTRACTOR_ID)
        when:
            this.contractorJobApplicationService.createContractorJob(NOT_EXISTING_PROJECT_ID, contractorJobDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createContractorJob should not create contractorJob if contractorJobDto validation fails"() {
        given:
            mockValidatingContractorJobDtoThrowsException()
            def contractorJobDto = this.prepareContractorJobDto(CONTRACTOR_ID)
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_WITHOUT_CONTRACTOR_JOB_ID,
                    contractorJobDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createContractorJob should not create contractorJob if validating contractor existence fails"() {
        given:
            mockValidatorContractorExistenceThrowsException()
            def contractorJobDto = this.prepareContractorJobDto(NOT_EXISING_CONTRACTOR_ID)
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_WITHOUT_CONTRACTOR_JOB_ID,
                    contractorJobDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createContractorJob should call save on projectRepository"() {
        given:
            def contractorJobDto = this.prepareContractorJobDto(CONTRACTOR_ID)
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_WITHOUT_CONTRACTOR_JOB_ID,
                    contractorJobDto)
        then:
            1 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createContractorJob should add newly created contractorJob to projectFinancialData"() {
        given:
            def contractorJobDto = this.prepareContractorJobDto(CONTRACTOR_ID)
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_WITHOUT_CONTRACTOR_JOB_ID,
                    contractorJobDto)
        then:
            1 * this.projectFinancialDataRepository.save({
                ProjectFinancialData financialData ->
                    financialData.contractorJobs.size() == 1
            })
    }

    def "createContractorJob should call onCreate on projectFinanceAwareObjectService"() {
        given:
            def contractorJobDto = this.prepareContractorJobDto(CONTRACTOR_ID)
        when:
            this.contractorJobApplicationService.createContractorJob(PROJECT_WITHOUT_CONTRACTOR_JOB_ID,
                    contractorJobDto)
        then:
            1 * this.projectFinanceAwareObjectService.onCreate(PROJECT_WITHOUT_CONTRACTOR_JOB_ID)
    }

    def "deleteContractorJob should not remove contractorJob if validating project existence fails"() {
        given:
        when:
            this.contractorJobApplicationService.deleteContractorJob(NOT_EXISTING_PROJECT_ID,
                    EXISTING_CONTRACTOR_JOB_ID)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "deleteContractorJob should not remove contractorJob if validating project having contractorJob fails"() {
        given:
            this.mockValidatingContractorJobOnProjectExistenceThrowsException()
        when:
            this.contractorJobApplicationService.deleteContractorJob(NOT_EXISTING_PROJECT_ID,
                    EXISTING_CONTRACTOR_JOB_ID)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "deleteContractorJob should remove contractorJob from projectFinancialData and save object via repository"() {
        given:
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID)
        then:
            1 * projectFinancialDataRepository.save({
                ProjectFinancialData projectFinancialData ->
                    projectFinancialData.contractorJobs.size() == 0
            })
    }

    def "deleteContractorJob should call onRemove on projectFinanceAwareObjectService"() {
        given:
        when:
            this.contractorJobApplicationService.deleteContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID)
        then:
            1 * this.projectFinanceAwareObjectService.onRemove(PROJECT_WITH_CONTRACTOR_JOB_ID)
    }

    def "updateContractorJob should not update contractorJob if validating project existence fails"() {
        given:
            def contractorJobDto = this.prepareContractorJobDtoForUpdate()
        when:
            this.contractorJobApplicationService.updateContractorJob(NOT_EXISTING_PROJECT_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateContractorJob should not update contractorJob if validation of contractorJob existence in project fails"() {
        given:
            this.mockValidatingContractorJobOnProjectExistenceThrowsException()
            def contractorJobDto = this.prepareContractorJobDtoForUpdate()
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateContractorJob should not update contractorJob if validation of contractorJobDto fails"() {
        given:
            this.mockValidatingUpdateContractorJobDtoThrowsException()
            def contractorJobDto = this.prepareContractorJobDtoForUpdate()
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateContractorJob should update contractorJob"() {
        given:
            def contractorJobDto = this.prepareContractorJobDtoForUpdate()
        when:
            def updatedContractorJob =
                    this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                            EXISTING_CONTRACTOR_JOB_ID, contractorJobDto)
        then:
            updatedContractorJob.name == NEW_NAME
            updatedContractorJob.value == NEW_VALUE
            updatedContractorJob.note == NEW_NOTE
    }

    def "updateContractorJob should save updated contractorJob"() {
        given:
            def contractorJobDto = this.prepareContractorJobDtoForUpdate()
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto)
        then:
            1 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateContractorJob should call onUpdate on projectFinanceAwareObjectService"() {
        given:
            def contractorJobDto = this.prepareContractorJobDtoForUpdate()
        when:
            this.contractorJobApplicationService.updateContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID,
                    EXISTING_CONTRACTOR_JOB_ID, contractorJobDto)
        then:
            1 * this.projectFinanceAwareObjectService.onUpdate(PROJECT_WITH_CONTRACTOR_JOB_ID)
    }

    def "getContractorJob should fail when project existence fails"() {
        given:
        when:
            def contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(NOT_EXISTING_PROJECT_ID, EXISTING_CONTRACTOR_JOB_ID)
        then:
            thrown(IllegalArgumentException)
            contractorJobDto == null
    }

    def "getContractorJob should call getContractorJobForProject on projectQueryService"() {
        given:
            mockValidationContractorJobExistenceFails()
        when:
            def contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID, NOT_EXISTING_CONTRACTOR_JOB_ID)
        then:
            thrown(IllegalArgumentException)
            contractorJobDto == null
    }

    def "getContractorJob should return contractorJobDto"() {
        given:
        when:
            def contractorJobDto = this.contractorJobApplicationService
                    .getContractorJob(PROJECT_WITH_CONTRACTOR_JOB_ID, EXISTING_CONTRACTOR_JOB_ID)
        then:
            contractorJobDto != null
    }

    private ContractorJobDto prepareContractorJobDto(Long contractorId) {
        def contractorJobDto = new ContractorJobDto(name: NAME, note: NOTE, value: VALUE,
                contractorId: contractorId, id: EXISTING_CONTRACTOR_JOB_ID, hasInvoice: true, payable: true)
        return contractorJobDto
    }

    private ContractorJobDto prepareContractorJobDtoForUpdate() {
        def contractorJobDto = new ContractorJobDto(name: NEW_NAME, note: NEW_NOTE, value: NEW_VALUE,
                contractorId: CONTRACTOR_ID, id: EXISTING_CONTRACTOR_JOB_ID, hasInvoice: true, payable: true)
        return contractorJobDto
    }

    private void mockValidatorContractorExistenceThrowsException() {
        contractorJobValidator.validateContractorExistence(NOT_EXISING_CONTRACTOR_ID)
                >> { throw new IllegalArgumentException() }
    }

    private void mockValidatingContractorJobDtoThrowsException() {
        contractorJobValidator.validateCreateContractorJobDto(_ as ContractorJobDto)
                >> { throw new IllegalArgumentException() }
    }

    private void mockValidatingContractorJobOnProjectExistenceThrowsException() {
        contractorJobValidator.validateContractorJobOnProjectExistence(_ as Long, _ as Long)
                >> { throw new IllegalArgumentException() }
    }

    private void mockValidatingUpdateContractorJobDtoThrowsException() {
        contractorJobValidator.validateUpdateContractorJobDto(_ as ContractorJobDto)
                >> { throw new IllegalArgumentException() }
    }

    private ProjectFinancialData prepareProjectFinancialDataWithContactorJob() {
        def projectFinancialData = new ProjectFinancialData(PROJECT_WITH_CONTRACTOR_JOB_ID)
        def contractorJob = new ContractorJob(NAME, CONTRACTOR_ID, VALUE, true, true)
        TestUtils.setFieldForObject(contractorJob, "id", EXISTING_CONTRACTOR_JOB_ID)
        projectFinancialData.addContractorJob(contractorJob)
        return projectFinancialData
    }

    private ProjectFinancialData prepareProjectFinancialDataWithoutContractorJob() {
        return new ProjectFinancialData(PROJECT_WITHOUT_CONTRACTOR_JOB_ID)
    }

    private void mockValidationContractorJobExistenceFails() {
        this.contractorJobValidator.validateContractorJobExistence(null, _ as Long, _ as Long)
                >> { throw new IllegalArgumentException() }
    }
}
