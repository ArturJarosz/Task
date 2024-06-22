package com.arturjarosz.task.finance.application.impl

import com.arturjarosz.task.common.mapper.MoneyMapperImpl
import com.arturjarosz.task.dto.InstallmentDto
import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService
import com.arturjarosz.task.finance.application.dto.FinancialValueDto
import com.arturjarosz.task.finance.application.mapper.InstallmentMapperImpl
import com.arturjarosz.task.finance.application.mapper.InstallmentProjectSummaryMapperImpl
import com.arturjarosz.task.finance.application.validator.InstallmentValidator
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository
import com.arturjarosz.task.finance.model.Installment
import com.arturjarosz.task.finance.model.PartialFinancialDataType
import com.arturjarosz.task.finance.model.ProjectFinancialData
import com.arturjarosz.task.finance.model.ProjectFinancialPartialData
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
    def installmentMapper = new InstallmentMapperImpl()
    def installmentProjectSummaryMapper = new InstallmentProjectSummaryMapperImpl()

    def installmentApplicationService = new InstallmentApplicationServiceImpl(projectValidator, stageValidator,
            projectFinancialDataRepository, financialDataQueryService, installmentValidator,
            projectFinanceAwareObjectService, installmentMapper, installmentProjectSummaryMapper)

    def setup() {
        def moneyMapper = new MoneyMapperImpl()
        TestUtils.setFieldForObject(this.installmentProjectSummaryMapper, "moneyMapper", moneyMapper)
        this.projectValidator.validateProjectExistence(NOT_EXISTING_PROJECT_ID) >> { throw new IllegalArgumentException() }
        this.stageValidator.validateExistenceOfStageInProject(_ as Long,
                NOT_EXISTING_STAGE_ID) >> { throw new IllegalArgumentException() }
        this.stageValidator.validateStageNotHavingInstallment(_ as Long,
                STAGE_WITH_INSTALLMENT_ID) >> { throw new IllegalArgumentException() }
        this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_ID) >> new ProjectFinancialData(PROJECT_ID)
        this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_WITH_INSTALLMENT_ID) >> prepareProjectFinancialDataWithInstallment(PROJECT_WITH_INSTALLMENT_ID, INSTALLMENT_ID)
        this.installmentValidator.validateInstallmentExistence(NOT_EXISTING_INSTALLMENT_ID) >> { throw new IllegalArgumentException() }
        this.financialDataQueryService.getInstallmentsByProjectId(PROJECT_WITH_INSTALLMENT_ID) >> ([new InstallmentDto()] as List)
        this.financialDataQueryService.getInstallmentDataForProject(PROJECT_WITH_INSTALLMENT_ID) >> prepareInstallmentData()
    }

    def "createInstallment should not create installment if project existence validation fails"() {
        given:
            def installmentDto = new InstallmentDto(value: VALUE)
        when:
            installmentApplicationService
                    .createInstallment(NOT_EXISTING_PROJECT_ID, STAGE_WITHOUT_INSTALLMENT_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createInstallment should not create installment if stage existence in project validation fails"() {
        given:
            def installmentDto = new InstallmentDto(value: VALUE)
        when:
            this.installmentApplicationService.createInstallment(PROJECT_ID, NOT_EXISTING_STAGE_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createInstallment should not create installment if stage not having installment validation fails"() {
        given:
            def installmentDto = new InstallmentDto(value: VALUE)
        when:
            this.installmentApplicationService.createInstallment(PROJECT_ID, STAGE_WITH_INSTALLMENT_ID,
                    installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createInstallment should not create installment if installmentDto validation fails"() {
        given:
            def installmentDto = new InstallmentDto()
            mockValidateInstallmentDtoThrowsException()
        when:
            this.installmentApplicationService.createInstallment(PROJECT_ID, STAGE_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createInstallment should add installment to projectFinancialData and return created installment"() {
        given:
            def installmentDto = new InstallmentDto(value: VALUE, hasInvoice: true)
        when:
            def createdInstallment =
                    this.installmentApplicationService.createInstallment(PROJECT_ID, STAGE_WITHOUT_INSTALLMENT_ID,
                            installmentDto)
        then:
            1 * this.projectFinancialDataRepository.save({ ProjectFinancialData projectFinancialData -> projectFinancialData.installments.size() == 1
            }) >> prepareProjectFinancialDataWithInstallment(PROJECT_ID, INSTALLMENT_ID)
        and:
            createdInstallment != null
            createdInstallment.value == VALUE
    }

    def "updateInstallment should not update installment if project existence validation fails"() {
        given:
            def installmentDto = new InstallmentDto(value: VALUE)
        when:
            this.installmentApplicationService.
                    updateInstallment(NOT_EXISTING_PROJECT_ID, STAGE_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateInstallment should not update installment if installment existence validation fails"() {
        given:
            def installmentDto = new InstallmentDto(value: VALUE)
        when:
            this.installmentApplicationService.
                    updateInstallment(PROJECT_ID, NOT_EXISTING_INSTALLMENT_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateInstallment should update installment if dto is correct and both project and installment exist"() {
        given:
            def installmentDto = new InstallmentDto(value: NEW_VALUE, hasInvoice: true)
        when:
            this.installmentApplicationService.
                    updateInstallment(PROJECT_WITH_INSTALLMENT_ID, INSTALLMENT_ID, installmentDto)
        then:
            1 * this.projectFinancialDataRepository.save({ ProjectFinancialData projectFinancialData ->
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
            1 * this.projectFinancialDataRepository.save({ ProjectFinancialData projectFinancialData -> projectFinancialData.installments.size() == 0
            })
    }

    def "payInstallment should not pay installment if project existence validation fails"() {
        given:
            def installmentDto = new InstallmentDto(paymentDate: LocalDate.now().minusDays(1))
        when:
            this.installmentApplicationService.payInstallment(NOT_EXISTING_PROJECT_ID,
                    INSTALLMENT_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "payInstallment should not pay installment if installmentDto validation fails"() {
        given:
            def installmentDto = new InstallmentDto(paymentDate: LocalDate.now().minusDays(1))
        when:
            this.installmentApplicationService.payInstallment(NOT_EXISTING_PROJECT_ID,
                    INSTALLMENT_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "payInstallment should change installment status to paid if dto is correct and both project and stage exist"() {
        given:
            def installmentDto = new InstallmentDto(paymentDate: LocalDate.now().minusDays(2))
        when:
            this.installmentApplicationService.payInstallment(PROJECT_WITH_INSTALLMENT_ID,
                    INSTALLMENT_ID, installmentDto)
        then:
            noExceptionThrown()
            1 * this.projectFinancialDataRepository.save({ ProjectFinancialData projectFinancialData -> projectFinancialData.installments.iterator().next().isPaid()
            })
    }

    def "payInstallment for not existing Installment should throw an exception"() {
        given:
            mockValidateInstallmentDtoThrowsException()
            def installmentDto = new InstallmentDto(paymentDate: LocalDate.now().minusDays(2))
        when:
            this.installmentApplicationService.payInstallment(PROJECT_WITH_INSTALLMENT_ID, NOT_EXISTING_INSTALLMENT_ID, installmentDto)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
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
            def installmentData =
                    this.installmentApplicationService.getProjectInstallments(PROJECT_WITH_INSTALLMENT_ID)
        then:
            noExceptionThrown()
            installmentData.financialData.count == 1
            installmentData.financialData.grossValue == 123.00d
            installmentData.financialData.netValue == 100.00d
            installmentData.financialData.vatTax == 23.00d
            installmentData.financialData.incomeTax == 19.00d
            installmentData.installments.size() == 1

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
            def resultInstallment =
                    this.installmentApplicationService.getInstallment(PROJECT_WITH_INSTALLMENT_ID, INSTALLMENT_ID)
        then:
            noExceptionThrown()
            resultInstallment.value == VALUE
    }

    private void mockValidateInstallmentDtoThrowsException() {
        this.installmentValidator.validateCreateInstallmentDto(_ as InstallmentDto) >> { throw new IllegalArgumentException() }
        this.installmentValidator.validateInstallmentExistence(null, _ as Long, _ as Long) >> { throw new IllegalArgumentException() }
    }

    private ProjectFinancialData prepareProjectFinancialDataWithInstallment(long projectId, long installmentId) {
        def installment = new Installment(new InstallmentDto(value: VALUE, paid: false, hasInvoice: true), STAGE_ID)
        TestUtils.setFieldForObject(installment, "id", installmentId)
        def financialData = new ProjectFinancialData(projectId)
        financialData.addInstallment(installment)
        return financialData
    }

    private ProjectFinancialPartialData prepareInstallmentData() {
        def financialValueDto = new FinancialValueDto()
        financialValueDto.setGrossValue(BigDecimal.valueOf(123.00d))
        financialValueDto.setNetValue(BigDecimal.valueOf(100.00d))
        financialValueDto.setVatTax(BigDecimal.valueOf(23.00d))
        financialValueDto.setIncomeTax(BigDecimal.valueOf(19.00d))

        return new ProjectFinancialPartialData(PartialFinancialDataType.INSTALLMENT, financialValueDto)
    }
}
