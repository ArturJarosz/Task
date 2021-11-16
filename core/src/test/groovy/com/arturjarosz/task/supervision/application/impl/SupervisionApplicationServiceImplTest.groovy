package com.arturjarosz.task.supervision.application.impl

import com.arturjarosz.task.finance.application.impl.ProjectFinancialDataServiceImpl
import com.arturjarosz.task.finance.model.FinancialData
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.model.AbstractEntity
import com.arturjarosz.task.sharedkernel.model.Money
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import com.arturjarosz.task.supervision.application.SupervisionValidator
import com.arturjarosz.task.supervision.application.SupervisionVisitValidator
import com.arturjarosz.task.supervision.application.dto.SupervisionDto
import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto
import com.arturjarosz.task.supervision.infrastructure.repository.impl.SupervisionRepositoryImpl
import com.arturjarosz.task.supervision.model.Supervision
import com.arturjarosz.task.supervision.model.SupervisionVisit
import com.arturjarosz.task.supervision.query.impl.SupervisionQueryServiceImpl
import com.arturjarosz.task.supervision.utils.FinancialDataBuilder
import com.arturjarosz.task.supervision.utils.SupervisionBuilder
import com.arturjarosz.task.supervision.utils.SupervisionVisitBuilder
import spock.lang.Specification

import java.time.LocalDate

class SupervisionApplicationServiceImplTest extends Specification {
    private static final Long PROJECT_ID = 1L;
    private static final Long NOT_EXISTING_PROJECT_ID = 2L;
    private static final Long SUPERVISION_ID = 10L;
    private static final Long SUPERVISION_VISIT_ID = 100L;
    private static final Long SUPERVISION_FINANCIAL_DATA_ID = 500L;
    private static final int HOURS_COUNT = 10;
    private static final int UPDATED_HOURS_COUNT = 11;
    private static final BigDecimal VISIT_NET_RATE = new BigDecimal("100");
    private static final BigDecimal UPDATED_VISIT_NET_RATE = new BigDecimal("101");
    private static final BigDecimal HOURLY_NET_RATE = new BigDecimal("100");
    private static final BigDecimal UPDATED_HOURLY_NET_RATE = new BigDecimal("102");
    private static final BigDecimal BASE_NET_RATE = new BigDecimal("100");
    private static final BigDecimal UPDATED_BASE_NET_RATE = new BigDecimal("103");
    private static final LocalDate DATE_OF_VISIT = new LocalDate(2021, 01, 01);
    private static final LocalDate UPDATED_DATE_OF_VISIT = new LocalDate(2021, 01, 01);

    def projectValidator = Mock(ProjectValidator);
    def supervisionValidator = Mock(SupervisionValidator);
    def supervisionVisitValidator = Mock(SupervisionVisitValidator);
    def supervisionRepository = Mock(SupervisionRepositoryImpl);
    def supervisionQueryService = Mock(SupervisionQueryServiceImpl);
    def projectFinancialDataApplicationService = Mock(ProjectFinancialDataServiceImpl);

    def supervisionApplicationService = new SupervisionApplicationServiceImpl(projectValidator, supervisionValidator,
            supervisionVisitValidator, supervisionRepository, supervisionQueryService,
            projectFinancialDataApplicationService
    );

    def "createSupervision should call validateCreateSupervision from supervisionValidator"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setHasInvoice(true);
            supervisionDto.setProjectId(PROJECT_ID);
        when:
            this.supervisionApplicationService.createSupervision(supervisionDto);
        then:
            1 * this.supervisionValidator.validateCreateSupervision(supervisionDto);
    }

    def "createSupervision should call validateProjectExistence from projectValidator"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setHasInvoice(true);
            supervisionDto.setProjectId(PROJECT_ID);
        when:
            this.supervisionApplicationService.createSupervision(supervisionDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "createSupervision throws an exception when passed supervisionDto is not valid and supervision is not saved"() {
        given:
            SupervisionDto supervisionDto = null;
            this.mockValidateCreateSupervisionDtoOnNull();
        when:
            this.supervisionApplicationService.createSupervision(supervisionDto);
        then:
            thrown IllegalArgumentException;
            0 * this.supervisionRepository.save(_ as Supervision);
    }

    def "createSupervision throws throws an exception when project does not exist and supervision is not saved"() {
        given:
            this.mockValidateProjectExistenceWithNotExistingProjectId();
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(NOT_EXISTING_PROJECT_ID);
        when:
            this.supervisionApplicationService.createSupervision(supervisionDto);
        then:
            thrown IllegalArgumentException;
            0 * this.supervisionRepository.save(_ as Supervision);
    }

    def "createSupervision should create supervision and save it with repository when passed dto is correct"() {
        given:
            SupervisionDto supervisionDto = this.prepareProperSupervisionDto();
        when:
            this.supervisionApplicationService.createSupervision(supervisionDto);
        then:
            noExceptionThrown();
            1 * this.supervisionRepository.save(_ as Supervision);
    }

    def "updateSupervision should call validateSupervisionExistence from supervisionValidator"() {
        given:
            SupervisionDto supervisionDto = this.prepareProperSupervisionDto();
            this.mockSupervisionRepositoryLoad();
        when:
            this.supervisionApplicationService.updateSupervision(SUPERVISION_ID, supervisionDto);
        then:
            1 * this.supervisionValidator.validateSupervisionExistence(SUPERVISION_ID);
    }

    def "updateSupervision should call validateUpdateSupervision from supervisionValidator"() {
        given:
            SupervisionDto supervisionDto = this.prepareProperSupervisionDto();
            this.mockSupervisionRepositoryLoad();
        when:
            this.supervisionApplicationService.updateSupervision(SUPERVISION_ID, supervisionDto);
        then:
            1 * this.supervisionValidator.validateUpdateSupervision(supervisionDto);
    }

    def "updateSupervision throws an exception when passed supervisionDto is not valid and supervision is not saved"() {
        given:
            SupervisionDto supervisionDto = null;
            this.mockValidateUpdateSupervisionDtoOnNull();
            this.mockSupervisionRepositoryLoad();
        when:
            this.supervisionApplicationService.updateSupervision(SUPERVISION_ID, supervisionDto);
        then:
            thrown IllegalArgumentException;
            0 * this.supervisionRepository.save(_ as Supervision);
    }

    def "updateSupervision should recalculated supervision FinancialData"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setHasInvoice(true);
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setVisitNetRate(UPDATED_VISIT_NET_RATE);
            supervisionDto.setHourlyNetRate(UPDATED_HOURLY_NET_RATE);
            supervisionDto.setBaseNetRate(UPDATED_BASE_NET_RATE);
            this.mockSupervisionRepositoryLoad();
        when:
            this.supervisionApplicationService.updateSupervision(SUPERVISION_ID, supervisionDto);
        then:
            1 * this.projectFinancialDataApplicationService.recalculateSupervision(SUPERVISION_ID, SUPERVISION_FINANCIAL_DATA_ID);
    }

    def "updateSupervision should save supervision with updated fields"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setHasInvoice(true);
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setVisitNetRate(UPDATED_VISIT_NET_RATE);
            supervisionDto.setHourlyNetRate(UPDATED_HOURLY_NET_RATE);
            supervisionDto.setBaseNetRate(UPDATED_BASE_NET_RATE);
            this.mockSupervisionRepositoryLoad();
        when:
            this.supervisionApplicationService.updateSupervision(SUPERVISION_ID, supervisionDto);
        then:
            1 * this.supervisionRepository.save({ Supervision supervision ->
                {
                    supervision.getVisitNetRate() == UPDATED_VISIT_NET_RATE;
                    supervision.getHourlyNetRate() == UPDATED_HOURLY_NET_RATE;
                    supervision.getBaseNetRate() == UPDATED_BASE_NET_RATE;
                }
            });
    }

    def "deleteSupervision should validate supervision existence"() {
        given:
        when:
            this.supervisionApplicationService.deleteSupervision(SUPERVISION_ID);
        then:
            1 * this.supervisionValidator.validateSupervisionExistence(SUPERVISION_ID);
    }

    def "deleteSupervision should call remove from supervisimocking project repository save onRepository "() {
        given:
        when:
            this.supervisionApplicationService.deleteSupervision(SUPERVISION_ID);
        then:
            1 * this.supervisionRepository.remove(SUPERVISION_ID);
    }

    def "getSupervision should return supervision loaded from supervisionRepository"() {
        given:
        when:
            this.supervisionApplicationService.getSupervision(SUPERVISION_ID);
        then:
            1 * this.supervisionRepository.load(SUPERVISION_ID);
    }

    def "createSupervisionVisit should validate Supervision existence"() {
        given:
            SupervisionVisitDto supervisionVisitDto = this.prepareProperSupervisionVisitDto();
            this.mockSupervisionRepositoryLoad();
            this.mockSupervisionRepositorySaveWithSupervisionVisit();
        when:
            this.supervisionApplicationService.createSupervisionVisit(SUPERVISION_ID, supervisionVisitDto);
        then:
            1 * this.supervisionValidator.validateSupervisionExistence(SUPERVISION_ID);
    }

    def "createSupervisionVisit should validate supervisionVisitDto"() {
        given:
            SupervisionVisitDto supervisionVisitDto = this.prepareProperSupervisionVisitDto();
            this.mockSupervisionRepositoryLoad();
            this.mockSupervisionRepositorySaveWithSupervisionVisit();
        when:
            this.supervisionApplicationService.createSupervisionVisit(SUPERVISION_ID, supervisionVisitDto);
        then:
            1 * this.supervisionVisitValidator.validateCreateSupervisionVisit(supervisionVisitDto);
    }

    def "createSupervisionVisit should recalculate supervision FinancialData"() {
        given:
            SupervisionVisitDto supervisionVisitDto = this.prepareProperSupervisionVisitDto();
            this.mockSupervisionRepositoryLoad();
            this.mockSupervisionRepositorySaveWithSupervisionVisit();
        when:
            this.supervisionApplicationService.createSupervisionVisit(SUPERVISION_ID, supervisionVisitDto);
        then:
            1 * this.projectFinancialDataApplicationService.recalculateSupervision(SUPERVISION_ID,
                    SUPERVISION_FINANCIAL_DATA_ID);
    }

    def "createSupervisionVisit should add new visit to supervision and saved with supervisionRepository"() {
        given:
            SupervisionVisitDto supervisionVisitDto = this.prepareProperSupervisionVisitDto();
            this.mockSupervisionRepositoryLoad();
            this.mockSupervisionRepositorySaveWithSupervisionVisit();
        when:
            this.supervisionApplicationService.createSupervisionVisit(SUPERVISION_ID, supervisionVisitDto);
        then:
            1 * this.supervisionRepository.save({
                Supervision supervision ->
                    supervision.getSupervisionVisits().size() == 1;
                    return supervision;
            } as Supervision) >> {
                arguments ->
                    Supervision supervisionToUpdate = arguments[0] as Supervision
                    supervisionToUpdate = this.injectMockedId(supervisionToUpdate, SUPERVISION_ID);
                    SupervisionVisit supervisionVisit = supervisionToUpdate.getSupervisionVisits().iterator().next();
                    this.injectMockedId(supervisionVisit, SUPERVISION_VISIT_ID);
                    return supervisionToUpdate;
            };
    }

    def "updateSupervisionVisit validates supervision existence"() {
        given:
            SupervisionVisitDto supervisionVisitDto = this.prepareProperSupervisionVisitDtoToUpdate();
            this.mockSupervisionRepositoryLoadWithSupervisionVisit();
            this.mockSupervisionRepositorySaveWithSupervisionVisit();
        when:
            this.supervisionApplicationService.updateSupervisionVisit(SUPERVISION_ID, SUPERVISION_VISIT_ID,
                    supervisionVisitDto)
        then:
            1 * this.supervisionValidator.validateSupervisionExistence(SUPERVISION_ID);
    }

    def "updateSupervisionVisit validates supervisionVisitDto"() {
        given:
            SupervisionVisitDto supervisionVisitDto = this.prepareProperSupervisionVisitDtoToUpdate();
            this.mockSupervisionRepositoryLoadWithSupervisionVisit();
            this.mockSupervisionRepositorySaveWithSupervisionVisit();
        when:
            this.supervisionApplicationService.updateSupervisionVisit(SUPERVISION_ID, SUPERVISION_VISIT_ID,
                    supervisionVisitDto)
        then:
            1 * this.supervisionVisitValidator.validateUpdateSupervisionVisit(supervisionVisitDto);
    }

    def "updateSupervisionVisit validates supervisionVisit presence on given supervision"() {
        given:
            SupervisionVisitDto supervisionVisitDto = this.prepareProperSupervisionVisitDtoToUpdate();
            this.mockSupervisionRepositoryLoadWithSupervisionVisit();
            this.mockSupervisionRepositorySaveWithSupervisionVisit();
        when:
            this.supervisionApplicationService.updateSupervisionVisit(SUPERVISION_ID, SUPERVISION_VISIT_ID,
                    supervisionVisitDto)
        then:
            1 * this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(SUPERVISION_ID,
                    SUPERVISION_VISIT_ID);
    }

    def "updateSupervisionVisit recalculated supervision FinancialData"() {
        given:
            SupervisionVisitDto supervisionVisitDto = this.prepareProperSupervisionVisitDtoToUpdate();
            this.mockSupervisionRepositoryLoadWithSupervisionVisit();
            this.mockSupervisionRepositorySaveWithSupervisionVisit();
        when:
            this.supervisionApplicationService.updateSupervisionVisit(SUPERVISION_ID, SUPERVISION_VISIT_ID,
                    supervisionVisitDto)
        then:
            1 * this.projectFinancialDataApplicationService.recalculateSupervision(SUPERVISION_ID, SUPERVISION_FINANCIAL_DATA_ID);
    }

    def "updateSupervisionVisit changes data on supervisionVisit"() {
        given:
            SupervisionVisitDto supervisionVisitDto = this.prepareProperSupervisionVisitDtoToUpdate();
            this.mockSupervisionRepositoryLoadWithSupervisionVisit();
        when:
            this.supervisionApplicationService.updateSupervisionVisit(SUPERVISION_ID, SUPERVISION_VISIT_ID,
                    supervisionVisitDto)
        then:
            1 * this.supervisionRepository.save({ Supervision supervision ->
                {
                    SupervisionVisit visit = supervision.supervisionVisits.iterator().next();
                    visit.getDateOfVisit() == UPDATED_DATE_OF_VISIT;

                }
            }) >> {
                arguments ->
                    Supervision supervisionToUpdate = arguments[0] as Supervision
                    SupervisionVisit supervisionVisit = supervisionToUpdate.getSupervisionVisits().iterator().next();
                    this.injectMockedId(supervisionVisit, SUPERVISION_VISIT_ID);
                    return supervisionToUpdate;
            }
    }

    def "deleteSupervisionVisit validates supervision existence"() {
        given:
            this.mockSupervisionRepositoryLoadWithSupervisionVisit();
        when:
            this.supervisionApplicationService.deleteSupervisionVisit(SUPERVISION_ID, SUPERVISION_VISIT_ID);
        then:
            1 * this.supervisionValidator.validateSupervisionExistence(SUPERVISION_ID);
    }

    def "deleteSupervisionVisit validates if supervisionVisit presence on given supervision"() {
        given:
            this.mockSupervisionRepositoryLoadWithSupervisionVisit();
        when:
            this.supervisionApplicationService.deleteSupervisionVisit(SUPERVISION_ID, SUPERVISION_VISIT_ID);
        then:
            1 * this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(SUPERVISION_ID,
                    SUPERVISION_VISIT_ID);
    }

    def "deleteSupervisionVisit recalculates supervision FinancialData"() {
        given:
            this.mockSupervisionRepositoryLoadWithSupervisionVisit();
        when:
            this.supervisionApplicationService.deleteSupervisionVisit(SUPERVISION_ID, SUPERVISION_VISIT_ID);
        then:
            1 * this.projectFinancialDataApplicationService.recalculateSupervision(SUPERVISION_ID, SUPERVISION_FINANCIAL_DATA_ID);
    }

    def "deleteSupervisionVisit removes supervisionVisit from supervision"() {
        given:
            this.mockSupervisionRepositoryLoadWithSupervisionVisit();
        when:
            this.supervisionApplicationService.deleteSupervisionVisit(SUPERVISION_ID, SUPERVISION_VISIT_ID);
        then:
            1 * this.supervisionRepository.save({ Supervision supervision ->
                {
                    supervision.supervisionVisits.size() == 0;
                }
            })
    }


    private void mockValidateCreateSupervisionDtoOnNull() {
        this.supervisionValidator.validateCreateSupervision(null) >> { throw new IllegalArgumentException() }
    }

    private void mockValidateUpdateSupervisionDtoOnNull() {
        this.supervisionValidator.validateUpdateSupervision(null) >> { throw new IllegalArgumentException() }
    }

    private void mockValidateProjectExistenceWithNotExistingProjectId() {
        this.projectValidator.validateProjectExistence(
                NOT_EXISTING_PROJECT_ID) >> { throw new IllegalArgumentException() }
    }

    private void mockSupervisionRepositoryLoad() {
        this.supervisionRepository.load(SUPERVISION_ID) >> this.createSupervisionWithFinancialData();
    }

    private void mockSupervisionRepositoryLoadWithSupervisionVisit() {
        this.supervisionRepository.load(SUPERVISION_ID) >> this.createSupervisionWithSupervisionVisit();

    }

    private Supervision createSupervisionWithFinancialData() {
        FinancialData financialData = new FinancialDataBuilder()
                .withId(SUPERVISION_FINANCIAL_DATA_ID)
                .withHasInvoice(true)
                .withPayable(true)
                .withValue(new Money(0))
                .build();
        return new SupervisionBuilder()
                .withFinancialData(financialData)
                .withHoursCount(0)
                .withBaseNetRate(new Money(BASE_NET_RATE))
                .withVisitNetRate(new Money(VISIT_NET_RATE))
                .withHourlyNetRate(new Money(HOURLY_NET_RATE))
                .withId(SUPERVISION_ID)
                .build();
    }

    private Supervision createSupervisionWithSupervisionVisit() {
        FinancialData financialData = new FinancialDataBuilder()
                .withId(SUPERVISION_FINANCIAL_DATA_ID)
                .withHasInvoice(true)
                .withPayable(true)
                .withValue(new Money(0))
                .build();
        SupervisionVisit supervisionVisit = new SupervisionVisitBuilder()
                .withId(SUPERVISION_VISIT_ID).withDateOfVisit(DATE_OF_VISIT).withHoursCount(HOURS_COUNT).withIsPayable(
                true).build();
        return new SupervisionBuilder()
                .withFinancialData(financialData)
                .withSupervisionVisit(supervisionVisit)
                .withId(SUPERVISION_ID)
                .withHoursCount(0)
                .withBaseNetRate(new Money(BASE_NET_RATE))
                .withVisitNetRate(new Money(VISIT_NET_RATE))
                .withHourlyNetRate(new Money(HOURLY_NET_RATE))
                .build();
    }

    private SupervisionDto prepareProperSupervisionDto() {
        SupervisionDto supervisionDto = new SupervisionDto();
        supervisionDto.setHasInvoice(true);
        supervisionDto.setProjectId(PROJECT_ID);
        supervisionDto.setHoursCount(HOURS_COUNT);
        supervisionDto.setVisitNetRate(VISIT_NET_RATE);
        supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
        supervisionDto.setBaseNetRate(BASE_NET_RATE);
        return supervisionDto;
    }

    private SupervisionVisitDto prepareProperSupervisionVisitDto() {
        SupervisionVisitDto supervisionVisitDto = new SupervisionVisitDto();
        supervisionVisitDto.setPayable(true);
        supervisionVisitDto.setHoursCount(HOURS_COUNT);
        supervisionVisitDto.setDateOfVisit(DATE_OF_VISIT);
        supervisionVisitDto.setSupervisionId(SUPERVISION_ID);
        return supervisionVisitDto;
    }

    private SupervisionVisitDto prepareProperSupervisionVisitDtoToUpdate() {
        SupervisionVisitDto supervisionVisitDto = new SupervisionVisitDto();
        supervisionVisitDto.setPayable(true);
        supervisionVisitDto.setHoursCount(UPDATED_HOURS_COUNT);
        supervisionVisitDto.setDateOfVisit(UPDATED_DATE_OF_VISIT);
        supervisionVisitDto.setSupervisionId(SUPERVISION_ID);
        return supervisionVisitDto;
    }

    private void mockSupervisionRepositorySaveWithSupervisionVisit() {
        this.supervisionRepository.save(_ as Supervision) >> {
            arguments ->
                Supervision supervisionToUpdate = arguments[0] as Supervision
                SupervisionVisit supervisionVisit = supervisionToUpdate.getSupervisionVisits().iterator().next();
                this.injectMockedId(supervisionVisit, SUPERVISION_VISIT_ID);
                return supervisionToUpdate;
        }
    }

    private AbstractEntity injectMockedId(AbstractEntity entity, Long id) {
        TestUtils.setFieldForObject(entity, "id", id);
        return entity;
    }
}
