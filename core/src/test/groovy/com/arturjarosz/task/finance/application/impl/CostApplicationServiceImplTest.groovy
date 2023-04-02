package com.arturjarosz.task.finance.application.impl

import com.arturjarosz.task.finance.application.validator.CostValidator
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository
import com.arturjarosz.task.finance.model.Cost
import com.arturjarosz.task.finance.model.CostCategory
import com.arturjarosz.task.finance.model.ProjectFinancialData
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.dto.CostDto
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

import java.time.LocalDate

class CostApplicationServiceImplTest extends Specification {

    private final static BigDecimal VALUE = new BigDecimal("100.0")
    private final static BigDecimal NEW_VALUE = new BigDecimal("120.0")
    private final static Long COST_ID = 100L;
    private final static Long NOT_EXISTING_COST_ID = 101L;
    private final static Long PROJECT_WITHOUT_COST_ID = 1L
    private final static Long NOT_EXISTING_PROJECT_ID = 2L
    private final static Long PROJECT_WITH_COST_ID = 3L
    private final static String NAME = "name"
    private final static String NEW_NAME = "newName"
    private final static String NOTE = "note"
    private final static String NEW_NOTE = "newNote"

    private final static LocalDate DATE = LocalDate.now()
    private final static LocalDate NEW_DATE = LocalDate.now().minusDays(20)

    def projectValidator = Mock(ProjectValidator)
    def costValidator = Mock(CostValidator)
    def projectFinanceAwareObjectService = Mock(ProjectFinanceAwareObjectServiceImpl)
    def projectFinancialDataRepository = Mock(ProjectFinancialDataRepository)
    def financialDataQueryService = Mock(FinancialDataQueryService)

    def projectCostApplicationService = new CostApplicationServiceImpl(costValidator, projectValidator,
            projectFinanceAwareObjectService, projectFinancialDataRepository,
            financialDataQueryService)

    def setup() {
        projectValidator.validateProjectExistence(NOT_EXISTING_PROJECT_ID) >> { throw new IllegalArgumentException() }
        projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_WITHOUT_COST_ID) >>
                prepareProjectFinancialDataWithoutCost(PROJECT_WITHOUT_COST_ID)
        projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_WITH_COST_ID) >>
                prepareProjectFinancialDataWithCost(PROJECT_WITH_COST_ID)
        costValidator.validateCostExistence(NOT_EXISTING_COST_ID) >> { throw new IllegalArgumentException() }
        financialDataQueryService.getCostById(COST_ID) >> this.prepareCostDto()
        financialDataQueryService.getCostsByProjectId(PROJECT_WITH_COST_ID) >> { this.prepareCostDto() as List }
    }

    def "createCost should not create cost if project existence validation fails"() {
        given:
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(NOT_EXISTING_PROJECT_ID, costDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createCost should not create cost if costDto validation fails"() {
        given:
            mockValidatingCreateCostDtoThrowsException()
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(PROJECT_WITHOUT_COST_ID, costDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createCost should call save on projectFinancialDataRepository"() {
        given:
            mockSaveProjectFinancialData()
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(PROJECT_WITHOUT_COST_ID, costDto)
        then:
            1 * this.projectFinancialDataRepository.save(_) >> this.prepareProjectFinancialDataWithCost()
    }

    def "createCost should add cost to project"() {
        given:
            mockSaveProjectFinancialData()
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(PROJECT_WITHOUT_COST_ID, costDto)
        then:
            1 * this.projectFinancialDataRepository.save({
                ProjectFinancialData financialData ->
                    financialData.costs.size() == 1
            }) >> this.prepareProjectFinancialDataWithCost()
    }

    def "createCost should call onCreate from projectFinanceAwareObjectService"() {
        given:
            mockSaveProjectFinancialData()
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(PROJECT_WITHOUT_COST_ID, costDto)
        then:
            1 * this.projectFinanceAwareObjectService.onCreate(PROJECT_WITHOUT_COST_ID)
    }

    def "getCost should not return cost if cost existence validation fails"() {
        given:
        when:
            def costDto = this.projectCostApplicationService.getCost(NOT_EXISTING_COST_ID)
        then:
            thrown(IllegalArgumentException)
            null == costDto
    }

    def "getCost should return costDto"() {
        given:
        when:
            def costDto = this.projectCostApplicationService.getCost(COST_ID)
        then:
            costDto != null
    }

    def "getCosts should not return costs if project existence validation fails"() {
        given:
        when:
            List<CostDto> costDtos = this.projectCostApplicationService.getCosts(NOT_EXISTING_PROJECT_ID)
        then:
            thrown(IllegalArgumentException)
            null == costDtos
    }

    def "getCosts should return list of costDto for project"() {
        given:
        when:
            def costDtos = this.projectCostApplicationService.getCosts(PROJECT_WITH_COST_ID)
        then:
            costDtos != null
    }

    def "deleteCost should not delete cost if project existence validation fails"() {
        given:
        when:
            this.projectCostApplicationService.deleteCost(NOT_EXISTING_PROJECT_ID, COST_ID)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "deleteCost should not delete cost if cost existence validation fails"() {
        given:
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, NOT_EXISTING_COST_ID)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "deleteCost remove cost from project"() {
        given:
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID)
        then:
            1 * this.projectFinancialDataRepository.save({
                ProjectFinancialData projectFinancialData ->
                    projectFinancialData.costs.size() == 0
            })
    }


    def "deleteCost should call onRemove on projectFinanceAwareObjectService"() {
        given:
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID)
        then:
            1 * this.projectFinanceAwareObjectService.onRemove(PROJECT_WITH_COST_ID)
    }

    def "updateCost should not update cost if project existence validation fails"() {
        given:
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            def updatedCost = this.projectCostApplicationService.updateCost(NOT_EXISTING_PROJECT_ID, COST_ID, costDto)
        then:
            thrown(IllegalArgumentException)
            null == updatedCost
    }

    def "updateCost should not update cost if cost existence validation fails"() {
        given:
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            def updatedCost =
                    this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, NOT_EXISTING_COST_ID, costDto)
        then:
            thrown(IllegalArgumentException)
            null == updatedCost
    }

    def "update should not update cost if costDto validation fails"() {
        given:
            CostDto costDto = this.prepareUpdateCostDto()
            mockValidatingUpdateCostThrowsException()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto)
        then:
            1 * this.costValidator.validateUpdateCostDto(_)
    }

    def "update cost should update cost data"() {
        given:
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto)
        then:
            1 * this.projectFinancialDataRepository.save({
                ProjectFinancialData financialData ->
                    Cost cost = financialData.costs.iterator().next()
                    cost.value == NEW_VALUE
                    cost.name == NEW_NAME
                    cost.note == NEW_NOTE
                    cost.date == NEW_DATE
            })
    }

    def "update cost should return updated cost"() {
        given:
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            def updatedCostDt = this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto)
        then:
            updatedCostDt.value == NEW_VALUE
            updatedCostDt.name == NEW_NAME
            updatedCostDt.note == NEW_NOTE
            updatedCostDt.date == NEW_DATE
    }

    def "update cost should call onUpdate on projectFinanceAwareObjectService"() {
        given:
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto)
        then:
            1 * this.projectFinanceAwareObjectService.onUpdate(PROJECT_WITH_COST_ID)
    }

    private CostDto prepareUpdateCostDto() {
        CostDto updateCostDto = new CostDto(note: NEW_NOTE, value: NEW_VALUE, date: NEW_DATE, name: NEW_NAME)
        return updateCostDto
    }

    private CostDto prepareCostDto() {
        CostDto costDto = new CostDto(name: NAME, date: DATE, category: CostCategory.FUEL, value: VALUE, note: NOTE)
        return costDto
    }

    private ProjectFinancialData prepareProjectFinancialDataWithCost(Long projectId) {
        ProjectFinancialData financialData = new ProjectFinancialData(projectId)
        def cost = new Cost(NAME, VALUE, CostCategory.FUEL, DATE, NOTE, true, true)
        TestUtils.setFieldForObject(cost, "id", COST_ID)
        financialData.addCost(cost)
        return financialData
    }

    private ProjectFinancialData prepareProjectFinancialDataWithoutCost(Long projectId) {
        ProjectFinancialData financialData = new ProjectFinancialData(projectId)
        return financialData
    }

    private void mockSaveProjectFinancialData(Long projectId) {
        ProjectFinancialData financialData = this.prepareProjectFinancialDataWithCost(projectId);
        this.projectFinancialDataRepository.save(_ as ProjectFinancialData) >> {
            Cost cost = financialData.costs.iterator().next()
            TestUtils.setFieldForObject(cost, "id", COST_ID)
            return financialData
        }
    }

    private void mockValidatingCreateCostDtoThrowsException() {
        this.costValidator.validateCostDto(_ as CostDto) >> { throw new IllegalArgumentException() }
    }

    private void mockValidatingUpdateCostThrowsException() {
        this.costValidator.validateUpdateCostDto(_ as CostDto) >> { throw new IllegalArgumentException() }
    }
}
