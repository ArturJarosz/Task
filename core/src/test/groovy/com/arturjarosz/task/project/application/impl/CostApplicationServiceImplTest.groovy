package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.dto.CostDto
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Cost
import com.arturjarosz.task.project.model.CostCategory
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto
import com.arturjarosz.task.sharedkernel.model.Money
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Field
import java.time.LocalDate

class CostApplicationServiceImplTest extends Specification {

    private final static Double VALUE = 100.0;
    private final static Long ARCHITECT_ID = 33L;
    private final static Long CLIENT_ID = 44L;
    private final static Long COST_ID = 100L;
    private final static Long EXISTING_PROJECT_ID = 1L;
    private final static Long NOT_EXISTING_PROJECT_ID = 900L;
    private final static String NAME = "name";
    private final static String DESCRIPTION = "description";
    private final static String PROJECT_NAME = "project";

    private final static LocalDate DATE = LocalDate.now();
    private final static CostCategory CATEGORY_FUEL = CostCategory.FUEL;
    private final static ProjectType PROJECT_TYPE_CONCEPT = ProjectType.CONCEPT;

    static final CostDto COST_DTO = new CostDto(NAME, CATEGORY_FUEL, VALUE, DATE, DESCRIPTION);
    static final Cost COST = new Cost(NAME, new Money(VALUE), CATEGORY_FUEL, DATE, DESCRIPTION);

    @Shared
    Project project = new Project(PROJECT_NAME, ARCHITECT_ID, CLIENT_ID, PROJECT_TYPE_CONCEPT);

    def projectRepository = Mock(ProjectRepositoryImpl) {
        load(NOT_EXISTING_PROJECT_ID) >> { null };
        load(EXISTING_PROJECT_ID) >> {
            return project;
        };
        save(project) >> {
            Field field = Cost.superclass.getDeclaredField("id");
            field.setAccessible(true);
            Cost cost = project.getCosts().iterator().next();
            field.set(cost, COST_ID);
        }
    }

    def projectValidator = new ProjectValidator(projectRepository);

    def projectQueryService = Mock(ProjectQueryServiceImpl) {
        getCostById(COST_ID) >> {
            Field field = Cost.superclass.getDeclaredField("id");
            field.setAccessible(true);
            field.set(COST, COST_ID);
            return COST;
        }
    }

    def projectCostApplicationService = new CostApplicationServiceImpl(projectValidator, projectRepository,
            projectQueryService);

    def cleanup() {
        //cleaning up project costs
        Field field = Project.getDeclaredField("costs");
        field.setAccessible(true);
        field.set(project, null)
    }

    def "when not existing projectId passed, createCost should throw an exception and cost should not be saved"() {
        given:
        when:
            this.projectCostApplicationService.createCost(NOT_EXISTING_PROJECT_ID, this.COST_DTO);
        then:
            Exception ex = thrown();
            ex.message == "notExists.project";
            0 * this.projectRepository.save(_);
    }

    def "when passing null as costDto, createCost should throw an exception and cost should not be saved"() {
        given:
            CostDto costDto = null;
        when:
            this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, costDto);
        then:
            Exception ex = thrown();
            ex.message == "isNull.cost";
            0 * this.projectRepository.save(_);
    }

    def "when passing existing project id and proper costDto, no exception thrown, cost is created and project is saved()"() {
        given:
        when:
            CreatedEntityDto createdEntityDto = this.projectCostApplicationService.createCost(EXISTING_PROJECT_ID, COST_DTO);
        then:
            noExceptionThrown();
            createdEntityDto.getId() == COST_ID;
    }

    def "when passing not existing costId exception should be thrown"() {
        given:
        when:
            this.projectCostApplicationService.getCost(NOT_EXISTING_PROJECT_ID);
        then:
            Exception ex = thrown();
            ex.message == "notExists.cost"
    }

    def "when passing existing costId no exception should be thrown and CostDto should be returned"() {
        given:
        when:
            CostDto costDto = this.projectCostApplicationService.getCost(COST_ID);
        then:
            noExceptionThrown();
            costDto.getId() == COST_ID;
    }

    def "getCosts should return list of all costs"() {
        given:
            project.addCost(COST);
        when:
            List<CostDto> costs = this.projectCostApplicationService.getCosts(EXISTING_PROJECT_ID);
        then:
            noExceptionThrown();
            costs.size() == 1;
    }


}
