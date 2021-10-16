package com.arturjarosz.task.finance.application.impl

import com.arturjarosz.task.finance.infrastructure.impl.FinancialDataRepositoryImpl
import com.arturjarosz.task.finance.infrastructure.impl.ProjectFinancialDataRepositoryImpl
import com.arturjarosz.task.finance.model.FinancialData
import com.arturjarosz.task.finance.model.ProjectFinancialData
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto
import com.arturjarosz.task.finance.query.impl.FinancialDataQueryServiceImpl
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.sharedkernel.model.Money
import spock.lang.Specification

class ProjectFinancialDataServiceImplTest extends Specification {
    private static final Long PROJECT_ID = 1L;
    private static final Long SUPERVISION_ID = 10L;
    private static final Long SUPERVISION_FINANCIAL_DATA_ID = 100L;
    private static final BigDecimal SUPERVISION_VALUE = new BigDecimal("1200.0");
    private static final BigDecimal BASE_NET_RATE = new BigDecimal(600.0);
    private static final int HOURS_COUNT_PAYABLE_VISIT = 4;
    private static final int HOURS_COUNT_NOT_PAYABLE_VISIT = 5;
    private static final BigDecimal HOURLY_RATE = new BigDecimal(100.0);
    private static final BigDecimal VISIT_RATE = new BigDecimal(200.0);


    def projectFinancialDataRepository = Mock(ProjectFinancialDataRepositoryImpl);
    def projectValidator = Mock(ProjectValidator);
    def financialDataQueryService = Mock(FinancialDataQueryServiceImpl);
    def financialDataRepository = Mock(FinancialDataRepositoryImpl);

    def projectFinancialDataApplicationService = new ProjectFinancialDataServiceImpl
            (projectFinancialDataRepository, projectValidator, financialDataQueryService, financialDataRepository);

    def "createProjectFinancialData should call validateProjectExistence on projectValidator"() {
        given:
        when:
            this.projectFinancialDataApplicationService.createProjectFinancialData(PROJECT_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "createProjectFinancialData should save projectFinancialData with repository"() {
        given:
        when:
            this.projectFinancialDataApplicationService.createProjectFinancialData(PROJECT_ID);
        then:
            1 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData);
    }

    def "createProjectFinancialData should return projectFinancialData with correct projectId"() {
        given:
            mockProjectFinancialDataRepositorySave();
        when:
            ProjectFinancialData projectFinancialData = this.projectFinancialDataApplicationService
                    .createProjectFinancialData(PROJECT_ID);
        then:
            projectFinancialData.getProjectId() == PROJECT_ID;
    }

    def "recalculateSupervision should recalculate financial data of supervision with only payable visits"() {
        given:
            this.mockFinancialDataGetSupervisionRates();
            this.mockFinancialDataQueryServiceGetVisitsFinancialDto();
            this.mockFinancialDataRepositoryLoad();
        when:
            this.projectFinancialDataApplicationService.recalculateSupervision(SUPERVISION_ID,
                    SUPERVISION_FINANCIAL_DATA_ID);
        then:
            1 * this.financialDataRepository.save({ FinancialData financialData ->
                financialData.getValue().hasSameValueAs(new Money(SUPERVISION_VALUE));
            })
    }

    private void mockProjectFinancialDataRepositorySave() {
        ProjectFinancialData projectFinancialData = new ProjectFinancialData(PROJECT_ID);
        1 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData) >> projectFinancialData;
    }

    private void mockFinancialDataGetSupervisionRates() {
        SupervisionRatesDto supervisionRatesDto = new SupervisionRatesDto();
        supervisionRatesDto.setHourlyNetRate(HOURLY_RATE);
        supervisionRatesDto.setBaseNetRate(BASE_NET_RATE);
        supervisionRatesDto.setVisitNetRate(VISIT_RATE);
        1 * this.financialDataQueryService.getSupervisionRatesDto(SUPERVISION_ID) >> supervisionRatesDto;
    }

    private void mockFinancialDataQueryServiceGetVisitsFinancialDto() {
        List<SupervisionVisitFinancialDto> supervisionVisitFinancialDtos = new ArrayList<>();
        SupervisionVisitFinancialDto supervisionVisitFinancialDto = new SupervisionVisitFinancialDto();
        supervisionVisitFinancialDto.setHoursCount(HOURS_COUNT_PAYABLE_VISIT);
        supervisionVisitFinancialDto.setPayable(true);
        supervisionVisitFinancialDtos.add(supervisionVisitFinancialDto);
        SupervisionVisitFinancialDto notPayableSupervisionVisitFinancialDto = new SupervisionVisitFinancialDto();
        notPayableSupervisionVisitFinancialDto.setHoursCount(HOURS_COUNT_NOT_PAYABLE_VISIT);
        notPayableSupervisionVisitFinancialDto.setPayable(false);
        supervisionVisitFinancialDtos.add(notPayableSupervisionVisitFinancialDto);
        1 * this.financialDataQueryService.getVisitsFinancialDto(SUPERVISION_ID) >> supervisionVisitFinancialDtos;
    }

    private void mockFinancialDataRepositoryLoad() {
        FinancialData financialData = new FinancialData();
        1 * this.financialDataRepository.load(SUPERVISION_FINANCIAL_DATA_ID) >> financialData;
    }

}
