package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.finance.application.impl.ProjectFinanceAwareObjectServiceImpl
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.SupplyValidator
import com.arturjarosz.task.project.application.dto.SupplyDto
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Supply
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

class SupplyApplicationServiceImplTest extends Specification {
    private static final String NAME = "name"
    private static final String NEW_NAME = "newName"
    private static final String NEW_NOTE = "newNote"
    private static final long PROJECT_ID = 1L
    private static final long SUPPLIER_ID = 10L
    private static final long SUPPLY_ID = 100L
    private static final BigDecimal VALUE = new BigDecimal("100.0")
    private static final BigDecimal NEW_VALUE = new BigDecimal("200.0")
    private static final boolean HAS_INVOICE = true
    private static final boolean NEW_HAS_INVOICE = false
    private static final boolean PAYABLE = true
    private static final boolean NEW_PAYABLE = false

    def projectQueryService = Mock(ProjectQueryServiceImpl)
    def projectRepository = Mock(ProjectRepository)
    def projectValidator = Mock(ProjectValidator)
    def supplyValidator = Mock(SupplyValidator)
    def projectFinanceAwareObjectService = Mock(ProjectFinanceAwareObjectServiceImpl)

    def supplyApplicationService = new SupplyApplicationServiceImpl(projectFinanceAwareObjectService,
            projectQueryService, projectRepository, projectValidator, supplyValidator)

    def "createSupply should validate project existence"() {
        given:
            this.mockProjectRepositoryLoad()
            SupplyDto supplyDto = this.prepareCreateSupplyDto()
        when:
            this.supplyApplicationService.createSupply(PROJECT_ID, supplyDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_ID)
    }

    def "createSupply should validate supply dto"() {
        given:
            this.mockProjectRepositoryLoad()
            SupplyDto supplyDto = this.prepareCreateSupplyDto()
        when:
            this.supplyApplicationService.createSupply(PROJECT_ID, supplyDto)
        then:
            1 * this.supplyValidator.validateCreateSupplyDto(supplyDto)
    }

    def "createSupply should validate supplier existence"() {
        given:
            this.mockProjectRepositoryLoad()
            SupplyDto supplyDto = this.prepareCreateSupplyDto()
        when:
            this.supplyApplicationService.createSupply(PROJECT_ID, supplyDto)
        then:
            1 * this.supplyValidator.validateSupplierExistence(SUPPLIER_ID)
    }

    def "createSupply should add supply to project"() {
        given:
            this.mockProjectRepositoryLoad()
            SupplyDto supplyDto = this.prepareCreateSupplyDto()
        when:
            this.supplyApplicationService.createSupply(PROJECT_ID, supplyDto)
        then:
            1 * this.projectRepository.save({
                Project project ->
                    project.supplies.size() == 1
            })
    }

    def "createSupply should call onCreate on projectFinanceAwareObjectService"() {
        given:
            this.mockProjectRepositoryLoad()
            SupplyDto supplyDto = this.prepareCreateSupplyDto()
        when:
            this.supplyApplicationService.createSupply(PROJECT_ID, supplyDto)
        then:
            1 * this.projectFinanceAwareObjectService.onCreate(PROJECT_ID)
    }

    def "updateSupply should validate project existence"() {
        given:
            this.mockProjectRepositoryLoadWithSupply()
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
        when:
            this.supplyApplicationService.updateSupply(PROJECT_ID, SUPPLY_ID, supplyDto)
        then:
            1 * this.projectValidator.validateProjectExistence(_ as Optional<Project>, PROJECT_ID)
    }

    def "updateSupply should validate supply on project existence"() {
        given:
            this.mockProjectRepositoryLoadWithSupply()
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
        when:
            this.supplyApplicationService.updateSupply(PROJECT_ID, SUPPLY_ID, supplyDto)
        then:
            1 * this.supplyValidator.validateSupplyOnProjectExistence(PROJECT_ID, SUPPLY_ID)
    }

    def "updateSupply should validate supply dto"() {
        given:
            this.mockProjectRepositoryLoadWithSupply()
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
        when:
            this.supplyApplicationService.updateSupply(PROJECT_ID, SUPPLY_ID, supplyDto)
        then:
            1 * this.supplyValidator.validateUpdateSupplyDto(supplyDto)
    }

    def "updateSupply should update data on supply"() {
        given:
            this.mockProjectRepositoryLoadWithSupply()
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
        when:
            SupplyDto updatedSupply = this.supplyApplicationService.updateSupply(PROJECT_ID, SUPPLY_ID, supplyDto)
        then:
            updatedSupply.note == NEW_NOTE
            updatedSupply.name == NEW_NAME
            updatedSupply.value == NEW_VALUE
            updatedSupply.hasInvoice == NEW_HAS_INVOICE
            updatedSupply.payable == NEW_PAYABLE
    }

    def "updateSupply should call onUpdate on projectFinanceAwareObjectService"() {
        given:
            this.mockProjectRepositoryLoadWithSupply()
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
        when:
            SupplyDto updatedSupply = this.supplyApplicationService.updateSupply(PROJECT_ID, SUPPLY_ID, supplyDto)
        then:
            1 * this.projectFinanceAwareObjectService.onUpdate(PROJECT_ID)
    }

    def "getSupply should validate project existence"() {
        given:
        when:
            this.supplyApplicationService.getSupply(PROJECT_ID, SUPPLY_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID)
    }

    def "getSupply should validate supply on project existence"() {
        given:
        when:
            this.supplyApplicationService.getSupply(PROJECT_ID, SUPPLY_ID)
        then:
            1 * this.supplyValidator.validateSupplyOnProjectExistence(PROJECT_ID, SUPPLY_ID)
    }

    def "getSupply should return correct supply"() {
        given:
            this.mockProjectQueryServiceGetSupplyForProject()
        when:
            SupplyDto supply = this.supplyApplicationService.getSupply(PROJECT_ID, SUPPLY_ID)
        then:
            supply.id == SUPPLY_ID
    }

    def "deleteSupply should validate project existence"() {
        given:
            this.mockProjectRepositoryLoadWithSupply()
        when:
            this.supplyApplicationService.deleteSupply(PROJECT_ID, SUPPLY_ID)
        then:
            1 * this.supplyValidator.validateSupplyOnProjectExistence(PROJECT_ID, SUPPLY_ID)
    }

    def "deleteSupply should validate supply on project existence"() {
        given:
            this.mockProjectRepositoryLoadWithSupply()
        when:
            this.supplyApplicationService.deleteSupply(PROJECT_ID, SUPPLY_ID)
        then:
            1 * this.supplyValidator.validateSupplyOnProjectExistence(PROJECT_ID, SUPPLY_ID)
    }

    def "deleteSupply should remove supply from project"() {
        given:
            this.mockProjectRepositoryLoadWithSupply()
        when:
            this.supplyApplicationService.deleteSupply(PROJECT_ID, SUPPLY_ID)
        then:
            1 * this.projectRepository.save({ Project project ->
                project.supplies.size() == 0
            })
    }

    def "deleteSupply should call onRemove on projectFinanceAwareObjectService"() {
        given:
            this.mockProjectRepositoryLoadWithSupply()
        when:
            this.supplyApplicationService.deleteSupply(PROJECT_ID, SUPPLY_ID)
        then:
            1 * this.projectFinanceAwareObjectService.onRemove(PROJECT_ID)
    }

    def "getSupplyForProject should call validateProjectExistence"() {
        given:
            this.mockProjectQueryServiceGetSuppliesForProject()
        when:
            this.supplyApplicationService.getSuppliesForProject(PROJECT_ID)
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID)
    }

    def "getSupplyForProject should return list of all supplies for project with given projectId"() {
        given:
            this.mockProjectQueryServiceGetSuppliesForProject()
        when:
            def listOfSupplies = this.supplyApplicationService.getSuppliesForProject(PROJECT_ID)
        then:
            listOfSupplies.size() == 1
    }


    private SupplyDto prepareCreateSupplyDto() {
        SupplyDto supplyDto = new SupplyDto(hasInvoice: HAS_INVOICE, payable: PAYABLE, supplierId: SUPPLIER_ID,
                value: VALUE)
        return supplyDto
    }

    private SupplyDto prepareUpdateSupplyDto() {
        SupplyDto supplyDto = new SupplyDto(hasInvoice: NEW_HAS_INVOICE, payable: NEW_PAYABLE, supplierId: SUPPLIER_ID,
                value: NEW_VALUE, name: NEW_NAME, note: NEW_NOTE)
        return supplyDto
    }

    private void mockProjectRepositoryLoad() {
        this.projectRepository.findById(PROJECT_ID) >> Optional.of(new ProjectBuilder()
                .withId(PROJECT_ID)
                .build())
    }

    private void mockProjectRepositoryLoadWithSupply() {
        this.projectRepository.findById(PROJECT_ID) >> Optional.of(new ProjectBuilder()
                .withId(PROJECT_ID)
                .withSupply(prepareSupply(SUPPLY_ID))
                .build())
    }

    private static Supply prepareSupply(long supplyId) {
        Supply supply = new Supply(NAME, SUPPLIER_ID, VALUE, true, true)
        TestUtils.setFieldForObject(supply, "id", supplyId)
        return supply
    }

    private void mockProjectQueryServiceGetSupplyForProject() {
        SupplyDto supplyDto = this.prepareCreateSupplyDto()
        supplyDto.setId(SUPPLY_ID)
        1 * this.projectQueryService.getSupplyForProject(SUPPLY_ID, PROJECT_ID) >> supplyDto
    }

    private void mockProjectQueryServiceGetSuppliesForProject() {
        SupplyDto supplyDto = this.prepareCreateSupplyDto()
        supplyDto.setId(SUPPLY_ID)
        1 * this.projectQueryService.getSuppliesForProject(PROJECT_ID) >> List.of(supplyDto)
    }
}
