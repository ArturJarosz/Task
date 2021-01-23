package com.arturjarosz.task.project.application

import com.arturjarosz.task.project.application.dto.CostDto
import com.arturjarosz.task.project.model.Cost
import com.arturjarosz.task.project.model.CostCategory
import spock.lang.Specification

import java.time.LocalDate

class CostValidatorTest extends Specification {

    private static final Long COST_ID = 1L;
    private static final String DESCRIPTION = "description";
    private static final String NAME = "cost_name";
    private static final Double NEGATIVE_VALUE = -1.0;
    private static final Double VALUE = 10.0;

    private static final CostCategory CATEGORY_FUEL = CostCategory.FUEL;
    private static final LocalDate DATE = LocalDate.now();


    def "when costDto is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = null;
        when:
            CostValidator.validateCostDto(costDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.cost";
    }

    def "when costDto category is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto(NAME, null, VALUE, DATE, DESCRIPTION);
        when:
            CostValidator.validateCostDto(costDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.cost.category";
    }

    def "when costDto date is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto(NAME, CATEGORY_FUEL, VALUE, null, DESCRIPTION);
        when:
            CostValidator.validateCostDto(costDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.cost.costDate";
    }

    def "when costDto name is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto(null, CATEGORY_FUEL, VALUE, DATE, DESCRIPTION);
        when:
            CostValidator.validateCostDto(costDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.cost.name";
    }

    def "when costDto name is empty, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto("", CATEGORY_FUEL, VALUE, DATE, DESCRIPTION);
        when:
            CostValidator.validateCostDto(costDto);
        then:
            Exception ex = thrown();
            ex.message == "isEmpty.cost.name";
    }

    def "when costData is provided, validateCostDto should not thrown any exception"() {
        given:
            CostDto costDto = new CostDto(NAME, CATEGORY_FUEL, VALUE, DATE, DESCRIPTION);
        when:
            CostValidator.validateCostDto(costDto);
        then:
            noExceptionThrown();
    }

    def "when cost has negative value, validateCostDto should throw an exception"() {
        given:
            CostDto costDto = new CostDto(NAME, CATEGORY_FUEL, NEGATIVE_VALUE, DATE, DESCRIPTION);
        when:
            CostValidator.validateCostDto(costDto);
        then:
            Exception ex = thrown();
            ex.message == "notValid.cost.negative";
    }

    def "when cost is null, validateCostExistence should throw an exception with specific error message"() {
        given:
            Cost cost = null;
        when:
            CostValidator.validateCostExistence(cost, COST_ID);
        then:
            Exception ex = thrown();
            ex.message == "notExists.cost";
    }

}
