package com.arturjarosz.task.finance.application

import com.arturjarosz.task.dto.CostCategoryDto
import com.arturjarosz.task.dto.CostDto
import com.arturjarosz.task.finance.application.validator.CostValidator
import com.arturjarosz.task.finance.model.Cost
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import spock.lang.Specification

import java.time.LocalDate

class CostValidatorTest extends Specification {

    static final Long COST_ID = 1L
    static final Long NOT_EXISTING_COST_ID = 2L
    static final String NOTE = "note"
    static final String NAME = "cost_name"
    static final BigDecimal NEGATIVE_VALUE = new BigDecimal("-1.0")
    static final BigDecimal VALUE = new BigDecimal("10.0")
    static final CostCategoryDto CATEGORY = CostCategoryDto.FUEL

    static final LocalDate DATE = LocalDate.now()

    def financialDataQueryService = Mock(FinancialDataQueryService)

    def costValidator = new CostValidator(financialDataQueryService)

    def setup() {
        this.financialDataQueryService.doesCostExistByCostId(COST_ID) >> true
        this.financialDataQueryService.doesCostExistByCostId(NOT_EXISTING_COST_ID) >> false
    }

    def "when costDto is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = null
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost"
    }

    def "when costDto category is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = new CostDto(name: NAME, value: VALUE, date: DATE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.category"
    }

    def "when costDto date is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = new CostDto(name: NAME, category: CATEGORY, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.costDate"
    }

    def "when costDto name is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = new CostDto(category: CATEGORY, date: DATE, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.name"
    }

    def "when costDto name is empty, validateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = new CostDto(name: "", category: CATEGORY, date: DATE, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.cost.name"
    }

    def "when costData is provided, validateCostDto should not thrown any exception"() {
        given:
            def costDto = new CostDto(name: NAME, category: CATEGORY, date: DATE, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            noExceptionThrown()
    }

    def "when cost has negative value, validateCostDto should throw an exception"() {
        given:
            def costDto = new CostDto(name: NAME, category: CATEGORY, date: DATE, value: NEGATIVE_VALUE,
                    note: NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "notValid.cost.negative"
    }

    def "when costDto is null, validateUpdateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = null
        when:
            this.costValidator.validateUpdateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost"
    }

    def "when costDto category is null, validateUpdateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = new CostDto(name: NAME, value: VALUE, date: DATE, note: NOTE)
        when:
            this.costValidator.validateUpdateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.category"
    }

    def "when costDto date is null, validateUpdateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = new CostDto(name: NAME, category: CATEGORY, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateUpdateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.costDate"
    }

    def "when costDto name is null, validateUpdateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = new CostDto(category: CATEGORY, date: DATE, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateUpdateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.name"
    }

    def "when costDto name is empty, validateUpdateCostDto should thrown an exception with specific error message"() {
        given:
            def costDto = new CostDto(name: "", category: CATEGORY, date: DATE, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateUpdateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.cost.name"
    }

    def "when costData is provided, validateUpdateCostDto should not thrown any exception"() {
        given:
            def costDto = new CostDto(name: NAME, category: CATEGORY, date: DATE, value: VALUE, note: NOTE)
        when:
            this.costValidator.validateUpdateCostDto(costDto)
        then:
            noExceptionThrown()
    }

    def "when cost has negative value, validateUpdateCostDto should throw an exception"() {
        given:
            def costDto = new CostDto(name: NAME, category: CATEGORY, date: DATE, value: NEGATIVE_VALUE,
                    note: NOTE)
        when:
            this.costValidator.validateUpdateCostDto(costDto)
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
