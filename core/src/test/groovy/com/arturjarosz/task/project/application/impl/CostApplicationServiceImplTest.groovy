package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.finance.application.impl.ProjectFinanceAwareObjectServiceImpl
import com.arturjarosz.task.project.application.CostValidator
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.dto.CostDto
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository
import com.arturjarosz.task.project.model.Cost
import com.arturjarosz.task.project.model.CostCategory
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

import java.time.LocalDate

class CostApplicationServiceImplTest extends Specification {

    private final static BigDecimal VALUE = new BigDecimal("100.0")
    private final static BigDecimal NEW_VALUE = new BigDecimal("120.0")
    private final static Long ARCHITECT_ID = 33L
    private final static Long CLIENT_ID = 44L
    private final static Long CONTRACT_ID = 55L;
    private final static Long COST_ID = 100L;
    private final static Long EXISTING_PROJECT_ID = 1L
    private final static Long NOT_EXISTING_PROJECT_ID = 900L
    private final static Long PROJECT_WITH_COST_ID = 2L
    private final static String NAME = "name"
    private final static String NEW_NAME = "newName"
    private final static String NOTE = "note"
    private final static String NEW_NOTE = "newNote"
    private final static String PROJECT_NAME = "project"

    private final static LocalDate DATE = LocalDate.now()
    private final static LocalDate NEW_DATE = LocalDate.now().minusDays(20)

    private final static ProjectWorkflow PROJECT_WORKFLOW = new ProjectWorkflow()

    def projectRepository = Mock(ProjectRepository)
    def projectValidator = Mock(ProjectValidator)
    def projectQueryService = Mock(ProjectQueryServiceImpl)
    def costValidator = Mock(CostValidator)
    def projectFinanceAwareObjectService = Mock(ProjectFinanceAwareObjectServiceImpl)

    def projectCostApplicationService = new CostApplicationServiceImpl(costValidator, projectValidator,
            projectRepository, projectQueryService, projectFinanceAwareObjectService)

    def "createCost should run validateProjectExistence"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepository()
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, EXISTING_PROJECT_ID)
    }

    def "createCost should run ValidateCostDto"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepository()
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto)
        then:
            1 * this.costValidator.validateCostDto(_)
    }

    def "createCost should call save on projectRepository"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepository()
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto)
        then:
            1 * this.projectRepository.save(_) >> this.prepareProjectWithCost()
    }

    def "createCost should add cost to project"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepository()
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto)
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.costs.size() == 1
            }) >> this.prepareProjectWithCost()
    }

    def "createCost should call onCreate from projectFinanceAwareObjectService"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepository()
            CostDto costDto = this.prepareCostDto()
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto)
        then:
            1 * this.projectFinanceAwareObjectService.onCreate(EXISTING_PROJECT_ID)
    }

    def "getCost should call validateCostExistence"() {
        given:
            this.mockProjectQueryService()
        when:
            this.projectCostApplicationService.getCost(COST_ID)
        then:
            1 * this.costValidator.validateCostExistence(_, _)
    }

    def "getCosts should call validateProjectExistence"() {
        given:
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.getCosts(PROJECT_WITH_COST_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_WITH_COST_ID)
    }

    def "getCosts should return list of projectCosts"() {
        given:
            this.mockProjectRepositoryForProjectWithCost()
        when:
            List<CostDto> costDtos = this.projectCostApplicationService.getCosts(PROJECT_WITH_COST_ID)
        then:
            costDtos.size() == 1
    }

    def "deleteCost should call validate projectExistence"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_COST_ID)
    }

    def "deleteCost should call validateCostExistence"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID)
        then:
            1 * this.costValidator.validateCostExistence(COST_ID)
    }

    def "deleteCost should call save on projectRepository"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID)
        then:
            1 * this.projectRepository.save(_)
    }

    def "deleteCost should remove cost from project"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID)
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.costs.size() == 0
            })
    }

    def "deleteCost should call onRemove on projectFinanceAwareObjectService"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID)
        then:
            1 * this.projectFinanceAwareObjectService.onRemove(PROJECT_WITH_COST_ID)
    }

    def "updateCost should call validateProjectExistence"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto)
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_COST_ID)
    }

    def "updateCost should call validateCostExistence"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto)
        then:
            1 * this.costValidator.validateCostExistence(COST_ID)
    }

    def "update cost should call validateUpdateCostDto"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto)
        then:
            1 * this.costValidator.validateUpdateCostDto(_)
    }

    def "updateCost should call save on projectRepository"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto)
        then:
            1 * this.projectRepository.save(_)
    }

    def "update cost should update cost data"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto)
        then:
            1 * this.projectRepository.save({
                Project project ->
                    Cost cost = project.costs.iterator().next()
                    cost.value == NEW_VALUE
                    cost.name == NEW_NAME
                    cost.note == NEW_NOTE
                    cost.date == NEW_DATE
            })
    }

    def "update cost should call onUpdate on projectFinanceAwareObjectService"() {
        given:
            this.mockProjectQueryService()
            this.mockProjectRepositoryForProjectWithCost()
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

    private Project prepareProjectWithNoCosts() {
        Project project = new Project(PROJECT_NAME, ARCHITECT_ID, CLIENT_ID, ProjectType.CONCEPT,
                PROJECT_WORKFLOW, CONTRACT_ID)
        return project
    }

    private Project prepareProjectWithCost() {
        Project project = new Project(PROJECT_NAME, ARCHITECT_ID, CLIENT_ID, ProjectType.CONCEPT,
                PROJECT_WORKFLOW, CONTRACT_ID)
        def cost = new Cost(NAME, VALUE, CostCategory.FUEL, DATE, NOTE, true, true)
        TestUtils.setFieldForObject(cost, "id", COST_ID)
        project.addCost(cost)
        return project

    }

    private void mockProjectRepository() {
        Project project = this.prepareProjectWithNoCosts()
        this.projectRepository.findById(NOT_EXISTING_PROJECT_ID) >> { Optional.ofNullable(null) }
        this.projectRepository.findById(EXISTING_PROJECT_ID) >> { Optional.of(project) }
        this.projectRepository.save(_ as Project) >> {
            Cost cost = project.costs.iterator().next()
            TestUtils.setFieldForObject(cost, "id", COST_ID)
            return project
        }
    }

    private void mockProjectRepositoryForProjectWithCost() {
        Project project = this.prepareProjectWithCost()
        this.projectRepository.findById(PROJECT_WITH_COST_ID) >> Optional.of(project)
    }

    private void mockProjectQueryService() {
        def cost = new Cost(NAME, VALUE, CostCategory.FUEL, DATE, NOTE, true, true)
        this.projectQueryService.getCostById(COST_ID) >> {
            TestUtils.setFieldForObject(cost, "id", COST_ID)
            return cost
        }
    }
}
