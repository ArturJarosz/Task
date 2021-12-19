package com.arturjarosz.task.supervision.application

import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto
import com.arturjarosz.task.supervision.query.impl.SupervisionQueryServiceImpl
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class SupervisionVisitValidatorTest extends Specification {
    private static final Long SUPERVISION_ID = 1L
    private static final LocalDate DATE_OF_VISIT = new LocalDate(2021, 10, 10)
    private static final int HOURS_COUNT = 10
    private static final int NEGATIVE_HOURS_COUNT = -10

    def supervisionQueryService = Mock(SupervisionQueryServiceImpl)

    def supervisionValidatorVisit = new SupervisionVisitValidator(supervisionQueryService)

    def "validateCreateSupervisionVisit throws an exception when supervisionVisitDto is null"() {
        given:
            SupervisionVisitDto supervisionVisitDto = null
        when:
            this.supervisionValidatorVisit.validateCreateSupervisionVisit(supervisionVisitDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.supervisionVisit"
    }

    @Unroll
    def "validateCreateSupervisionVisit throws an exception with proper exception code"() {
        given:
            SupervisionVisitDto supervisionVisitDto = new SupervisionVisitDto()
            supervisionVisitDto.supervisionId = supervisionId
            supervisionVisitDto.dateOfVisit = dateOfVisit
            supervisionVisitDto.payable = isPayable
            supervisionVisitDto.hoursCount = hoursCount
        when:
            this.supervisionValidatorVisit.validateCreateSupervisionVisit(supervisionVisitDto)
        then:
            Exception ex = thrown()
            ex.message == exceptionMessage
        where:
            supervisionId  | dateOfVisit   | isPayable | hoursCount           || exceptionMessage
            SUPERVISION_ID | DATE_OF_VISIT | null      | HOURS_COUNT          || "isNull.supervisionVisit.payableFlag"
            SUPERVISION_ID | null          | true      | HOURS_COUNT          || "isNull.supervisionVisit.dateOfVisit"
            SUPERVISION_ID | DATE_OF_VISIT | true      | null                 || "isNull.supervisionVisit.hoursCount"
            SUPERVISION_ID | DATE_OF_VISIT | true      | NEGATIVE_HOURS_COUNT || "negative.supervisionVisit.hoursCount"


    }

}
