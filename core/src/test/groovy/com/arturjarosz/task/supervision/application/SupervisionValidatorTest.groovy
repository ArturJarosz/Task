package com.arturjarosz.task.supervision.application


import com.arturjarosz.task.supervision.application.dto.SupervisionDto
import com.arturjarosz.task.supervision.query.impl.SupervisionQueryServiceImpl
import spock.lang.Specification

class SupervisionValidatorTest extends Specification {

    private static final Long PROJECT_ID = 1L;
    private static final Long SUPERVISION_ID = 10L;
    private static final BigDecimal BASE_NET_RATE = new BigDecimal(100.00D);
    private static final BigDecimal NEGATIVE_RATE = new BigDecimal(-200.00D);
    private static final BigDecimal HOURLY_NET_RATE = new BigDecimal(300.00D);
    private static final BigDecimal VISIT_NET_RATE = new BigDecimal(400.00D);

    def supervisionQueryService = Mock(SupervisionQueryServiceImpl);

    SupervisionValidator supervisionValidator = new SupervisionValidator(supervisionQueryService);

    def "validateCreateSupervision throws an exception when passed supervisionDto is null"() {
        given:
        when:
            this.supervisionValidator.validateCreateSupervision(null);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision";
    }

    def "validateCreateSupervision throws an exception when passed supervisionDto has no projectId"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(null);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateCreateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision.projectId";
    }

    def "validateCreateSupervision throws an exception when passed supervisionDto has no hasInvoice flag"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateCreateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision.invoiceFlag";
    }

    def "validateCreateSupervision throws an exception when passed supervision has no baseNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(null);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateCreateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision.baseNetRate";
    }

    def "validateCreateSupervision throws an exception when passed supervision has negative baseNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(NEGATIVE_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateCreateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "negative.supervision.baseNetRate";
    }

    def "validateCreateSupervision throws an exception when passed supervision has no hourlyNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(null);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateCreateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision.hourlyNetRate";
    }

    def "validateCreateSupervision throws an exception when passed supervision has negative hourlyNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(NEGATIVE_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateCreateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "negative.supervision.hourlyNetRate";
    }

    def "validateCreateSupervision throws an exception when passed supervision has no visitNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(null);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateCreateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision.visitNetRate";
    }

    def "validateCreateSupervision throws an exception when passed supervision has negative visitNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(NEGATIVE_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateCreateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "negative.supervision.visitNetRate";
    }

    def "validateCreateSupervision will not throw an exception when supervisionDto has complete data"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateCreateSupervision(supervisionDto);
        then:
            noExceptionThrown();
    }

    def "validateUpdateSupervision throws an exception when passed supervisionDto has no hasInvoice flag"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision.invoiceFlag";
    }

    def "validateUpdateSupervision throws an exception when passed supervision has no baseNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(null);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision.baseNetRate";
    }

    def "validateUpdateSupervision throws an exception when passed supervision has negative baseNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(NEGATIVE_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "negative.supervision.baseNetRate";
    }

    def "validateUpdateSupervision throws an exception when passed supervision has no hourlyNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(null);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision.hourlyNetRate";
    }

    def "validateUpdateSupervision throws an exception when passed supervision has negative hourlyNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(NEGATIVE_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "negative.supervision.hourlyNetRate";
    }

    def "validateUpdateSupervision throws an exception when passed supervision has no visitNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(null);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.supervision.visitNetRate";
    }

    def "validateUpdateSupervision throws an exception when passed supervision has negative visitNetRate"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(NEGATIVE_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        then:
            Exception ex = thrown();
            ex.message == "negative.supervision.visitNetRate";
    }

    def "validateUpdateSupervision will not throw an exception when supervisionDto has complete data"() {
        given:
            SupervisionDto supervisionDto = new SupervisionDto();
            supervisionDto.setProjectId(PROJECT_ID);
            supervisionDto.setBaseNetRate(BASE_NET_RATE);
            supervisionDto.setHasInvoice(true);
            supervisionDto.setHourlyNetRate(HOURLY_NET_RATE);
            supervisionDto.setVisitNetRate(VISIT_NET_RATE);
            supervisionDto.setHoursCount(0);
        when:
            this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        then:
            noExceptionThrown();
    }

    def "validateSupervisionExistence will throw an exception if there is not supervision with supervisionId"() {
        given:
        when:
            this.supervisionValidator.validateSupervisionExistence(SUPERVISION_ID);
        then:
            Exception ex = thrown();
            ex.message == "notExist.supervision";
    }

    private void mockSupervisionQueryServiceSupervisionExistsFalse() {
        this.supervisionQueryService.supervisionExists(SUPERVISION_ID) >> false;
    }


}
