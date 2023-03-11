package com.arturjarosz.task.finance.application

import com.arturjarosz.task.finance.model.Cost
import com.arturjarosz.task.finance.model.CostCategory
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.project.application.dto.CostDto
import spock.lang.Specification

import java.time.LocalDate

class CostValidatorTest extends Specification {

    private static final Long COST_ID = 1L
    private static final Long NOT_EXISTING_COST_ID = 2L
    private static final String NOTE = "note"
    private static final String NAME = "cost_name"
    private static final BigDecimal NEGATIVE_VALUE = new BigDecimal("-1.0")
    private static final BigDecimal VALUE = new BigDecimal("10.0")

    private static final LocalDate DATE = LocalDate.now()

    def financialDataQueryService = Mock(FinancialDataQueryService)

    def costValidator = new CostValidator(financialDataQueryService)

    def setup() {
        this.financialDataQueryService.doesCostExistByCostId(COST_ID) >> true
        this.financialDataQueryService.doesCostExistByCostId(NOT_EXISTING_COST_ID) >> false
    }

    def "when costDto is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = null
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost"
    }

    def "when costDto category is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto(name: NAME, value: VALUE, date: DATE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.category"
    }

    def "when costDto date is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto(name: NAME, category: CostCategory.FUEL, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.costDate"
    }

    def "when costDto name is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto(category: CostCategory.FUEL, date: DATE, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.name"
    }

    def "when costDto name is empty, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto(name: "", category: CostCategory.FUEL, date: DATE, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.cost.name"
    }

    def "when costData is provided, validateCostDto should not thrown any exception"() {
        given:
            CostDto costDto = new CostDto(name: NAME, category: CostCategory.FUEL, date: DATE, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            noExceptionThrown()
    }

    def "when cost has negative value, validateCostDto should throw an exception"() {
        given:
            CostDto costDto = new CostDto(name: NAME, category: CostCategory.FUEL, date: DATE, value: NEGATIVE_VALUE,
                    note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "notValid.cost.negative"
    }

    def "when cost is null, validateCostExistence should throw an exception with specific error message"() {
        given:
            Cost cost = null
        when:
            this.costValidator.validateCostExistence(cost, COST_ID)
        then:
            Exception ex = thrown()
            ex.message == "notExist.cost"
    }

    def "with not existing cost id, validateCostExistence should throw an exception with specific error message"() {
        given:
        when:
            this.costValidator.validateCostExistence(NOT_EXISTING_COST_ID)
        then:
            Exception ex = thrown()
            ex.message == "notExist.cost"
    }

    def "with existing cost id, validateCostExistence should not throw any exception"() {
        given:
        when:
            this.costValidator.validateCostExistence(COST_ID)
        then:
            noExceptionThrown()
    }
}
