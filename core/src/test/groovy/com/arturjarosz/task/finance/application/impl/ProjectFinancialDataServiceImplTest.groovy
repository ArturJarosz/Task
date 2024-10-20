package com.arturjarosz.task.finance.application.impl

import com.arturjarosz.task.dto.TotalProjectFinancialSummaryDto
import com.arturjarosz.task.finance.application.dto.FinancialValueDto
import com.arturjarosz.task.finance.domain.PartialFinancialDataService
import com.arturjarosz.task.finance.infrastructure.FinancialDataRepository
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository
import com.arturjarosz.task.finance.model.FinancialData
import com.arturjarosz.task.finance.model.PartialFinancialDataType
import com.arturjarosz.task.finance.model.ProjectFinancialData
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.model.Money
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

class ProjectFinancialDataServiceImplTest extends Specification {
    static final Long PROJECT_ID = 1L
    static final Long NOT_EXISTING_PROJECT_ID = 2L
    static final Long SUPERVISION_ID = 10L
    static final Long SUPERVISION_FINANCIAL_DATA_ID = 100L
    static final Long PROJECT_FINANCIAL_DATA_ID = 1000L
    static final BigDecimal SUPERVISION_VALUE = new BigDecimal("1200.0")
    static final BigDecimal BASE_NET_RATE = new BigDecimal("600.0")
    static final int HOURS_COUNT_PAYABLE_VISIT = 4
    static final int HOURS_COUNT_NOT_PAYABLE_VISIT = 5
    static final BigDecimal HOURLY_RATE = new BigDecimal("100.0")
    static final BigDecimal VISIT_RATE = new BigDecimal("200.0")
    static final BigDecimal COST_GROSS_VALUE = new BigDecimal("100")
    static final BigDecimal COST_NET_VALUE = new BigDecimal("80")
    static final BigDecimal COST_VAT_TAX_VALUE = new BigDecimal("15")
    static final BigDecimal COST_INCOME_TAX_VALUE = new BigDecimal("5")

    def projectFinancialDataRepository = Mock(ProjectFinancialDataRepository)
    def projectValidator = Mock(ProjectValidator)
    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl)
    def financialDataRepository = Mock(FinancialDataRepository)
    def partialFinancialDataService = Mock(PartialFinancialDataService)
    def partialFinancialDataServices = [partialFinancialDataService]

    def setup() {
        this.partialFinancialDataService.getType() >> PartialFinancialDataType.COST
        projectFinancialDataService = new ProjectFinancialDataServiceImpl
                (projectFinancialDataRepository, projectValidator, financialDataQueryService, financialDataRepository,
                        partialFinancialDataServices)
    }

    def projectFinancialDataService

    def "createProjectFinancialData should call validateProjectExistence on projectValidator"() {
        given:
            projectFinancialDataService = new ProjectFinancialDataServiceImpl
                    (projectFinancialDataRepository, projectValidator, financialDataQueryService, financialDataRepository,
                            [])
            projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_ID) >> new ProjectFinancialData(PROJECT_ID)
        when:
            this.projectFinancialDataService.createProjectFinancialData(PROJECT_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID)
    }

    def "createProjectFinancialData should save projectFinancialData with repository"() {
        given:
            projectFinancialDataService = new ProjectFinancialDataServiceImpl
                    (projectFinancialDataRepository, projectValidator, financialDataQueryService, financialDataRepository,
                            [])
            projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_ID) >> new ProjectFinancialData(PROJECT_ID)
        when:
            this.projectFinancialDataService.createProjectFinancialData(PROJECT_ID)
        then:
            1 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createProjectFinancialData should return projectFinancialData with correct projectId"() {
        given:
            projectFinancialDataService = new ProjectFinancialDataServiceImpl
                    (projectFinancialDataRepository, projectValidator, financialDataQueryService, financialDataRepository,
                            [])
            projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_ID) >> new ProjectFinancialData(PROJECT_ID)
            mockProjectFinancialDataRepositorySave()
        when:
            def projectFinancialData = this.projectFinancialDataService.createProjectFinancialData(PROJECT_ID)
        then:
            projectFinancialData.projectId == PROJECT_ID
    }

    def "recalculateSupervision should recalculate financial data of supervision with only payable visits"() {
        given:
            this.mockFinancialDataGetSupervisionRates()
            this.mockFinancialDataQueryServiceGetVisitsFinancialDto()
            this.mockFinancialDataRepositoryLoad()
        when:
            this.projectFinancialDataService.recalculateSupervision(SUPERVISION_ID,
                    SUPERVISION_FINANCIAL_DATA_ID)
        then:
            1 * this.financialDataRepository.save({ FinancialData financialData ->
                financialData.value.hasSameValueAs(new Money(SUPERVISION_VALUE))
            })
    }

    def "recalculateProjectFinancialData should recalculate financial data for given project"() {
        given:
            this.mockLoadProjectFinancialDataWithProjectId()
            this.mockCostProvidePartialFinancialData()
        when:
            this.projectFinancialDataService.recalculateProjectFinancialData(PROJECT_ID)
        then:
            1 * this.projectFinancialDataRepository.save({ ProjectFinancialData projectFinancialData ->
                projectFinancialData.partialSummaries[PartialFinancialDataType.COST].grossValue == new Money(100)
                projectFinancialData.partialSummaries[PartialFinancialDataType.COST].netValue == new Money(80)
                projectFinancialData.partialSummaries[PartialFinancialDataType.COST].vatTax == new Money(15)
                projectFinancialData.partialSummaries[PartialFinancialDataType.COST].incomeTax == new Money(5)
            })
    }

    def "getTotalProjectFinancialData does not return project financial data for not existing project"() {
        given:
            this.mockValidateProjectExistenceOnNotExistingProject()
        when:
            def projectFinancialDataDto =
                    this.projectFinancialDataService.getTotalProjectFinancialData(NOT_EXISTING_PROJECT_ID)
        then:
            thrown(Exception)
            projectFinancialDataDto == null
    }

    def "getTotalProjectFinancialData returns project financial data for existing project"() {
        given:
            this.mockLoadTotalProjectFinancialData()
        when:
            def projectFinancialDataDto =
                    this.projectFinancialDataService.getTotalProjectFinancialData(PROJECT_ID)
        then:
            projectFinancialDataDto != null
    }

    private void mockProjectFinancialDataRepositorySave() {
        def projectFinancialData = new ProjectFinancialData(PROJECT_ID)
        1 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData) >> projectFinancialData
    }

    private void mockFinancialDataGetSupervisionRates() {
        def supervisionRatesDto = new SupervisionRatesDto()
        supervisionRatesDto.hourlyNetRate = HOURLY_RATE
        supervisionRatesDto.baseNetRate = BASE_NET_RATE
        supervisionRatesDto.visitNetRate = VISIT_RATE
        1 * this.financialDataQueryService.getSupervisionRatesDto(SUPERVISION_ID) >> supervisionRatesDto
    }

    private void mockFinancialDataQueryServiceGetVisitsFinancialDto() {
        List<SupervisionVisitFinancialDto> supervisionVisitFinancialDtos = new ArrayList<>()
        def supervisionVisitFinancialDto = new SupervisionVisitFinancialDto()
        supervisionVisitFinancialDto.hoursCount = HOURS_COUNT_PAYABLE_VISIT
        supervisionVisitFinancialDto.payable = true
        supervisionVisitFinancialDtos.add(supervisionVisitFinancialDto)
        def notPayableSupervisionVisitFinancialDto = new SupervisionVisitFinancialDto()
        notPayableSupervisionVisitFinancialDto.hoursCount = HOURS_COUNT_NOT_PAYABLE_VISIT
        notPayableSupervisionVisitFinancialDto.payable = false
        supervisionVisitFinancialDtos.add(notPayableSupervisionVisitFinancialDto)
        1 * this.financialDataQueryService.getVisitsFinancialDto(SUPERVISION_ID) >> supervisionVisitFinancialDtos
    }

    private void mockFinancialDataRepositoryLoad() {
        def financialData = new FinancialData(new Money(0), true, true)
        1 * this.financialDataRepository.getReferenceById(SUPERVISION_FINANCIAL_DATA_ID) >> financialData
    }

    private void mockLoadProjectFinancialDataWithProjectId() {
        this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_ID) >>
                this.buildProjectFinancialData(PROJECT_ID, PROJECT_FINANCIAL_DATA_ID)
    }

    private ProjectFinancialData buildProjectFinancialData(long projectId, long id) {
        def projectFinancialData = new ProjectFinancialData(projectId)
        TestUtils.setFieldForObject(projectFinancialData, "id", id)
        return projectFinancialData
    }

    private void mockCostProvidePartialFinancialData() {
        def costsValue = new FinancialValueDto(grossValue: COST_GROSS_VALUE, netValue: COST_NET_VALUE,
                vatTax: COST_VAT_TAX_VALUE, incomeTax: COST_INCOME_TAX_VALUE)
        partialFinancialDataService.getPartialFinancialData(PROJECT_ID) >> costsValue
    }

    private void mockValidateProjectExistenceOnNotExistingProject() {
        this.projectValidator.validateProjectExistence(
                NOT_EXISTING_PROJECT_ID) >> { throw new IllegalArgumentException() }
    }

    private void mockLoadTotalProjectFinancialData() {
        this.financialDataQueryService.getTotalProjectFinancialSummary(PROJECT_ID) >> new TotalProjectFinancialSummaryDto()
    }
}
