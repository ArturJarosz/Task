package com.arturjarosz.task.finance.application.impl

import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService
import com.arturjarosz.task.finance.application.dto.InstallmentDto
import com.arturjarosz.task.finance.application.validator.InstallmentValidator
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository
import com.arturjarosz.task.finance.model.Installment
import com.arturjarosz.task.finance.model.ProjectFinancialData
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.StageValidator
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

import java.time.LocalDate

class InstallmentApplicationServiceImplTest extends Specification {

    static final Long INSTALLMENT_ID = 1L
    static final Long NOT_EXISTING_INSTALLMENT_ID = 2L
    static final Long STAGE_ID = 10L
    static final Long STAGE_WITH_INSTALLMENT_ID = 20L
    static final Long STAGE_WITHOUT_INSTALLMENT_ID = 30L
    static final Long NOT_EXISTING_STAGE_ID = 99L
    static final Long PROJECT_ID = 100L
    static final Long PROJECT_WITH_INSTALLMENT_ID = 200L
    static final Long NOT_EXISTING_PROJECT_ID = 999L
    static final BigDecimal VALUE = new BigDecimal("100.00")
    static final BigDecimal NEW_VALUE = new BigDecimal("200.00")

    def projectFinancialDataRepository = Mock(ProjectFinancialDataRepository)
    def financialDataQueryService = Mock(FinancialDataQueryService)
    def installmentValidator = Mock(InstallmentValidator)
    def projectValidator = Mock(ProjectValidator)
    def stageValidator = Mock(StageValidator)
    def projectFinanceAwareObjectService = Mock(ProjectFinanceAwareObjectService)

    def installmentApplicationService = new InstallmentApplicationServiceImpl(projectValidator, stageValidator,
            projectFinancialDataRepository, financialDataQueryService,
            installmentValidator, projectFinanceAwareObjectService)

    def setup() {
        this.projectValidator.validateProjectExistence(
                NOT_EXISTING_PROJECT_ID) >> { throw new IllegalArgumentException() }
        this.stageValidator.validateExistenceOfStageInProject(_ as Long,
                NOT_EXISTING_STAGE_ID) >> { throw new IllegalArgumentException() }
        this.stageValidator.validateStageNotHavingInstallment(_ as Long,
                STAGE_WITH_INSTALLMENT_ID) >> { throw new IllegalArgumentException() }
        this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_ID) >> new ProjectFinancialData(
                PROJECT_ID)
        this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_WITH_INSTALLMENT_ID) >>
                prepareProjectFinancialDataWithInstallment(PROJECT_WITH_INSTALLMENT_ID, INSTALLMENT_ID)
        this.installmentValidator.validateInstallmentExistence(NOT_EXISTING_INSTALLMENT_ID) >>
                { throw new IllegalArgumentException() }
        this.financialDataQueryService.getInstallmentsByProjectId(
                PROJECT_WITH_INSTALLMENT_ID) >> ([new InstallmentDto()] as List)
    }

    def "createInstallment should not create installment if project existence validation fails"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(value: VALUE)
        when:
            installmentApplicationService
                    .createInstallment(NOT_EXISTING_PROJECT_ID, STAGE_WITHOUT_INSTALLMENT_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createInstallment should not create installment if stage existence in project validation fails"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(value: VALUE)
        when:
            this.installmentApplicationService.createInstallment(PROJECT_ID, NOT_EXISTING_STAGE_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createInstallment should not create installment if stage not having installment validation fails"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(value: VALUE)
        when:
            this.installmentApplicationService.createInstallment(PROJECT_ID, STAGE_WITH_INSTALLMENT_ID,
                    installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createInstallment should not create installment if installmentDto validation fails"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto()
            mockValidateInstallmentDtoThrowsException()
        when:
            this.installmentApplicationService.createInstallment(PROJECT_ID, STAGE_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createInstallment should add installment to projectFinancialData and return created installment"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(value: VALUE, hasInvoice: true)
        when:
            def createdInstallment =
                    this.installmentApplicationService.createInstallment(PROJECT_ID, STAGE_WITHOUT_INSTALLMENT_ID,
                            installmentDto)
        then:
            1 * this.projectFinancialDataRepository.save({
                ProjectFinancialData projectFinancialData -> projectFinancialData.installments.size() == 1
            }) >> prepareProjectFinancialDataWithInstallment(PROJECT_ID, INSTALLMENT_ID)
        and:
            createdInstallment != null
            createdInstallment.value == VALUE
    }

    def "updateInstallment should not update installment if project existence validation fails"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(value: VALUE)
        when:
            this.installmentApplicationService.
                    updateInstallment(NOT_EXISTING_PROJECT_ID, STAGE_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateInstallment should not update installment if installment existence validation fails"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(value: VALUE)
        when:
            this.installmentApplicationService.
                    updateInstallment(PROJECT_ID, NOT_EXISTING_INSTALLMENT_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateInstallment should update installment if dto is correct and both project and installment exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(value: NEW_VALUE, hasInvoice: true)
        when:
            this.installmentApplicationService.
                    updateInstallment(PROJECT_WITH_INSTALLMENT_ID, INSTALLMENT_ID, installmentDto)
        then:
            1 * this.projectFinancialDataRepository.save({
                ProjectFinancialData projectFinancialData ->
                    def updatedInstallment = projectFinancialData.installments.iterator().next()
                    updatedInstallment.amount.value == NEW_VALUE
            }) >> prepareProjectFinancialDataWithInstallment(PROJECT_WITH_INSTALLMENT_ID, INSTALLMENT_ID)
    }

    def "removeInstallment should not remove installment if project existence validation fails"() {
        given:
        when:
            this.installmentApplicationService.removeInstallment(NOT_EXISTING_PROJECT_ID, INSTALLMENT_ID)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)

    }

    def "removeInstallment should remove installment from projectFinancialData if both project and installment exist"() {
        given:
        when:
            this.installmentApplicationService.removeInstallment(PROJECT_WITH_INSTALLMENT_ID,
                    INSTALLMENT_ID)
        then:
            1 * this.projectFinancialDataRepository.save({
                ProjectFinancialData projectFinancialData ->
                    projectFinancialData.installments.size() == 0
            })
    }

    def "payInstallment should not pay installment if project existence validation fails"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(paymentDate: LocalDate.now().minusDays(1))
        when:
            this.installmentApplicationService.payInstallment(NOT_EXISTING_PROJECT_ID,
                    INSTALLMENT_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "payInstallment should not pay installment if installmentDto validation fails"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(paymentDate: LocalDate.now().minusDays(1))
        when:
            this.installmentApplicationService.payInstallment(NOT_EXISTING_PROJECT_ID,
                    INSTALLMENT_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "payInstallment should change installment status to paid if dto is correct and both project and stage exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto(paymentDate: LocalDate.now().minusDays(2))
        when:
            this.installmentApplicationService.payInstallment(PROJECT_WITH_INSTALLMENT_ID,
                    INSTALLMENT_ID, installmentDto)
        then:
            noExceptionThrown()
            1 * this.projectFinancialDataRepository.save({
                ProjectFinancialData projectFinancialData ->
                    projectFinancialData.installments.iterator().next().isPaid()
            })
    }

    def "getProjectInstallments throw an exception and not return installments if project existence validation fails"() {
        given:
        when:
            List<InstallmentDto> installments =
                    this.installmentApplicationService.getProjectInstallments(NOT_EXISTING_PROJECT_ID)
        then:
            thrown(IllegalArgumentException)
            null == installments
    }

    def "getInstallmentList should return list of all installments for given project"() {
        given:
        when:
            List<InstallmentDto> installments =
                    this.installmentApplicationService.getProjectInstallments(PROJECT_WITH_INSTALLMENT_ID)
        then:
            noExceptionThrown()
            installments.size() == 1
    }

    def "getInstallment should throw an and not return installment if project existence validation fails"() {
        given:
        when:
            this.installmentApplicationService.getInstallment(NOT_EXISTING_PROJECT_ID, STAGE_WITH_INSTALLMENT_ID)
        then:
            thrown(IllegalArgumentException)
    }

    def "getInstallment should return installment when both project and installment exist"() {
        given:
        when:
            InstallmentDto resultInstallment =
                    this.installmentApplicationService.getInstallment(PROJECT_WITH_INSTALLMENT_ID, INSTALLMENT_ID)
        then:
            noExceptionThrown()
            resultInstallment.value == VALUE
    }

    private void mockValidateInstallmentDtoThrowsException() {
        this.installmentValidator.validateCreateInstallmentDto(_ as InstallmentDto)
                >> { throw new IllegalArgumentException() }
    }

    private ProjectFinancialData prepareProjectFinancialDataWithInstallment(long projectId, long installmentId) {
        def installment = new Installment(new InstallmentDto(value: VALUE, paid: false, hasInvoice: true), STAGE_ID)
        TestUtils.setFieldForObject(installment, "id", installmentId)
        def financialData = new ProjectFinancialData(projectId)
        financialData.addInstallment(installment)
        return financialData
    }
}
