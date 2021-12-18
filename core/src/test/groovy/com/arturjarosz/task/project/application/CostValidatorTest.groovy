package com.arturjarosz.task.project.application

import com.arturjarosz.task.project.application.dto.CostDto
import com.arturjarosz.task.project.model.Cost
import com.arturjarosz.task.project.model.CostCategory
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
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

    def projectQueryService = Mock(ProjectQueryServiceImpl)

    def costValidator = new CostValidator(projectQueryService)

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
            CostDto costDto = new CostDto()
            costDto.setNote(NAME)
            costDto.setValue(VALUE)
            costDto.setDate(DATE)
            costDto.setNote(NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.category"
    }

    def "when costDto date is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto()
            costDto.setNote(NAME)
            costDto.setCategory(CostCategory.FUEL)
            costDto.setValue(VALUE)
            costDto.setNote(NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.costDate"
    }

    def "when costDto name is null, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto()
            costDto.setCategory(CostCategory.FUEL)
            costDto.setDate(DATE)
            costDto.setValue(VALUE)
            costDto.setNote(NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isNull.cost.name"
    }

    def "when costDto name is empty, validateCostDto should thrown an exception with specific error message"() {
        given:
            CostDto costDto = new CostDto()
            costDto.setName("")
            costDto.setCategory(CostCategory.FUEL)
            costDto.setDate(DATE)
            costDto.setValue(VALUE)
            costDto.setNote(NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            Exception ex = thrown()
            ex.message == "isEmpty.cost.name"
    }

    def "when costData is provided, validateCostDto should not thrown any exception"() {
        given:
            CostDto costDto = new CostDto()
            costDto.setName(NAME)
            costDto.setCategory(CostCategory.FUEL)
            costDto.setDate(DATE)
            costDto.setValue(VALUE)
            costDto.setNote(NOTE)
        when:
            this.costValidator.validateCostDto(costDto)
        then:
            noExceptionThrown()
    }

    def "when cost has negative value, validateCostDto should throw an exception"() {
        given:
            CostDto costDto = new CostDto()
            costDto.setName(NAME)
            costDto.setCategory(CostCategory.FUEL)
            costDto.setDate(DATE)
            costDto.setValue(NEGATIVE_VALUE)
            costDto.setNote(NOTE)
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
            this.projectQueryService.getCostById(NOT_EXISTING_COST_ID) >> null
        when:
            this.costValidator.validateCostExistence(COST_ID)
        then:
            Exception ex = thrown()
            ex.message == "notExist.cost"
    }

    def "with existing cost id, validateCostExistence should not throw any exception"() {
        given:
            Cost cost = new Cost(NAME, VALUE, CostCategory.FUEL, DATE, NOTE, true, true)
            this.projectQueryService.getCostById(COST_ID) >> cost
        when:
            this.costValidator.validateCostExistence(COST_ID)
        then:
            noExceptionThrown()
    }

}
