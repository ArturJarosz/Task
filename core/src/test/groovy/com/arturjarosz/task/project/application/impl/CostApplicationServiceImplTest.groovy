package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.finance.application.ProjectFinancialDataService
import com.arturjarosz.task.project.application.CostValidator
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.dto.CostDto
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Cost
import com.arturjarosz.task.project.model.CostCategory
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.status.project.ProjectWorkflow
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import spock.lang.Specification

import java.time.LocalDate

class CostApplicationServiceImplTest extends Specification {

    private final static BigDecimal VALUE = new BigDecimal(100.0);
    private final static BigDecimal NEW_VALUE = new BigDecimal(120.0);
    private final static Long ARCHITECT_ID = 33L;
    private final static Long CLIENT_ID = 44L;
    private final static Long COST_ID = 100L;
    private final static Long EXISTING_PROJECT_ID = 1L;
    private final static Long NOT_EXISTING_PROJECT_ID = 900L;
    private final static Long PROJECT_WITH_COST_ID = 2L;
    private final static String NAME = "name";
    private final static String NEW_NAME = "newName";
    private final static String NOTE = "note";
    private final static String NEW_NOTE = "newNote";
    private final static String PROJECT_NAME = "project";

    private final static LocalDate DATE = LocalDate.now();
    private final static LocalDate NEW_DATE = LocalDate.now().minusDays(20);

    private final static ProjectWorkflow PROJECT_WORKFLOW = new ProjectWorkflow();

    def projectRepository = Mock(ProjectRepositoryImpl);

    def projectValidator = Mock(ProjectValidator);

    def projectQueryService = Mock(ProjectQueryServiceImpl);

    def costValidator = Mock(CostValidator);

    def projectFinancialDataService = Mock(ProjectFinancialDataService);

    def projectCostApplicationService = new CostApplicationServiceImpl(costValidator, projectValidator,
            projectRepository, projectQueryService, projectFinanceAwareObjectService);

    def "createCost should run validateProjectExistence"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepository();
            CostDto costDto = this.prepareCostDto();
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto);
        then:
            1 * this.projectValidator.validateProjectExistence(_);
    }

    def "createCost should run ValidateCostDto"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepository();
            CostDto costDto = this.prepareCostDto();
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto);
        then:
            1 * this.costValidator.validateCostDto(_);
    }

    def "createCost should call save on projectRepository"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepository();
            CostDto costDto = this.prepareCostDto();
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto);
        then:
            1 * this.projectRepository.save(_) >> this.prepareProjectWithCost();
    }

    def "createCost should add cost to project"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepository();
            CostDto costDto = this.prepareCostDto();
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.getCosts().size() == 1;
            }) >> this.prepareProjectWithCost();
    }

    def "createCost should call projectFinancialDataRecalculation"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepository();
            CostDto costDto = this.prepareCostDto();
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto);
        then:
            1 * this.projectFinancialDataService.recalculateProjectFinancialData(EXISTING_PROJECT_ID);
    }

    def "getCost should call validateCostExistence"() {
        given:
            this.mockProjectQueryService();
        when:
            this.projectCostApplicationService.getCost(COST_ID);
        then:
            1 * this.costValidator.validateCostExistence(_, _);
    }

    def "getCosts should call validateProjectExistence"() {
        given:
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.getCosts(PROJECT_WITH_COST_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_COST_ID);
    }

    def "getCosts should return list of projectCosts"() {
        given:
            this.mockProjectRepositoryForProjectWithCost()
        when:
            List<CostDto> costDtos = this.projectCostApplicationService.getCosts(PROJECT_WITH_COST_ID);
        then:
            costDtos.size() == 1;
    }

    def "deleteCost should call validate projectExistence"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_COST_ID);
    }

    def "deleteCost should call validateCostExistence"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID);
        then:
            1 * this.costValidator.validateCostExistence(COST_ID);
    }

    def "deleteCost should call save on projectRepository"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID);
        then:
            1 * this.projectRepository.save(_);
    }

    def "deleteCost should remove cost from project"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.getCosts().size() == 0;
            });
    }

    def "deleteCost should trigger projectFinancialData recalculation"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost()
        when:
            this.projectCostApplicationService.deleteCost(PROJECT_WITH_COST_ID, COST_ID);
        then:
            1 * this.projectFinancialDataService.recalculateProjectFinancialData(PROJECT_WITH_COST_ID);
    }

    def "updateCost should call validateProjectExistence"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost();
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_WITH_COST_ID);
    }

    def "updateCost should call validateCostExistence"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost();
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto);
        then:
            1 * this.costValidator.validateCostExistence(COST_ID);
    }

    def "update cost should call validateUpdateCostDto"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost();
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto);
        then:
            1 * this.costValidator.validateUpdateCostDto(_);
    }

    def "updateCost should call save on projectRepository"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost();
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto);
        then:
            1 * this.projectRepository.save(_);
    }

    def "update cost should update cost data"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost();
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto);
        then:
            1 * this.projectRepository.save({
                Project project ->
                    Cost cost = project.getCosts().iterator().next();
                    cost.getValue() == NEW_VALUE;
                    cost.getName() == NEW_NAME;
                    cost.getNote() == NEW_NOTE;
                    cost.getDate() == NEW_DATE;
            });
    }

    def "update cost should trigger projectFinancialData recalculate"() {
        given:
            this.mockProjectQueryService();
            this.mockProjectRepositoryForProjectWithCost();
            CostDto costDto = this.prepareUpdateCostDto()
        when:
            this.projectCostApplicationService.updateCost(PROJECT_WITH_COST_ID, COST_ID, costDto);
        then:
            1 * this.projectFinancialDataService.recalculateProjectFinancialData(PROJECT_WITH_COST_ID);
    }

    private CostDto prepareUpdateCostDto() {
        CostDto updateCostDto = new CostDto();
        updateCostDto.setNote(NEW_NOTE);
        updateCostDto.setValue(NEW_VALUE);
        updateCostDto.setDate(NEW_DATE);
        updateCostDto.setName(NEW_NAME);
        return updateCostDto;
    }

    private CostDto prepareCostDto() {
        CostDto costDto = new CostDto();
        costDto.setName(NAME);
        costDto.setDate(DATE);
        costDto.setCategory(CostCategory.FUEL);
        costDto.setValue(VALUE);
        costDto.setNote(NOTE);
        return costDto;
    }

    private Project prepareProjectWithNoCosts() {
        Project project = new Project(PROJECT_NAME, ARCHITECT_ID, CLIENT_ID, ProjectType.CONCEPT,
                PROJECT_WORKFLOW); ;
        return project;
    }

    private Project prepareProjectWithCost() {
        Project project = new Project(PROJECT_NAME, ARCHITECT_ID, CLIENT_ID, ProjectType.CONCEPT,
                PROJECT_WORKFLOW); ;
        def cost = new Cost(NAME, VALUE, CostCategory.FUEL, DATE, NOTE, true, true);
        TestUtils.setFieldForObject(cost, "id", COST_ID);
        project.addCost(cost);
        return project;

    }

    private void mockProjectRepository() {
        Project project = this.prepareProjectWithNoCosts();
        this.projectRepository.load(NOT_EXISTING_PROJECT_ID) >> { null };
        this.projectRepository.load(EXISTING_PROJECT_ID) >> {
            return project;
        };
        this.projectRepository.save(_ as Project) >> {
            Cost cost = project.getCosts().iterator().next();
            TestUtils.setFieldForObject(cost, "id", COST_ID);
            return project;
        }
    }

    private void mockProjectRepositoryForProjectWithCost() {
        Project project = this.prepareProjectWithCost();
        this.projectRepository.load(PROJECT_WITH_COST_ID) >> project;
    }

    private void mockProjectQueryService() {
        def cost = new Cost(NAME, VALUE, CostCategory.FUEL, DATE, NOTE, true, true);
        this.projectQueryService.getCostById(COST_ID) >> {
            TestUtils.setFieldForObject(cost, "id", COST_ID);
            return cost;
        }
    }
}
