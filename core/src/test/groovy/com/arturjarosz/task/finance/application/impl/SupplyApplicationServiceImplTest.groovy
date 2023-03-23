package com.arturjarosz.task.finance.application.impl

import com.arturjarosz.task.finance.application.SupplyValidator
import com.arturjarosz.task.finance.application.dto.SupplyDto
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository
import com.arturjarosz.task.finance.model.ProjectFinancialData
import com.arturjarosz.task.finance.model.Supply
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

class SupplyApplicationServiceImplTest extends Specification {
    private static final String NAME = "name"
    private static final String NEW_NAME = "newName"
    private static final String NEW_NOTE = "newNote"
    private static final long PROJECT_WITH_SUPPLY_ID = 1L
    private static final long PROJECT_WITHOUT_SUPPLY_ID = 2L
    private static final long NOT_EXISTING_PROJECT_ID = 3L
    private static final long SUPPLIER_ID = 10L
    private static final long NOT_EXISTING_SUPPLIER_ID = 11L
    private static final long SUPPLY_ID = 100L
    private static final BigDecimal VALUE = new BigDecimal("100.0")
    private static final BigDecimal NEW_VALUE = new BigDecimal("200.0")
    private static final boolean HAS_INVOICE = true
    private static final boolean NEW_HAS_INVOICE = false
    private static final boolean PAYABLE = true
    private static final boolean NEW_PAYABLE = false

    def projectValidator = Mock(ProjectValidator)
    def supplyValidator = Mock(SupplyValidator)
    def projectFinanceAwareObjectService = Mock(ProjectFinanceAwareObjectServiceImpl)
    def projectFinancialDataRepository = Mock(ProjectFinancialDataRepository)
    def financialDataQueryService = Mock(FinancialDataQueryService)

    def supplyApplicationService = new SupplyApplicationServiceImpl(projectFinanceAwareObjectService, projectValidator,
            supplyValidator, projectFinancialDataRepository,
            financialDataQueryService)

    def setup() {
        projectValidator.validateProjectExistence(NOT_EXISTING_PROJECT_ID) >> { throw new IllegalArgumentException() }
        supplyValidator.validateSupplierExistence(NOT_EXISTING_SUPPLIER_ID) >> { throw new IllegalArgumentException() }
        supplyValidator.validateSupplyOnProjectExistence(PROJECT_WITHOUT_SUPPLY_ID, SUPPLY_ID) >> {
            throw new IllegalArgumentException()
        }
        projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_WITHOUT_SUPPLY_ID) >>
                prepareProjectFinancialDataWithoutSupply()
        projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_WITH_SUPPLY_ID) >>
                prepareProjectFinancialDataWithSupply()
        financialDataQueryService.getSupplyById(SUPPLY_ID) >> prepareSupplyDto(SUPPLY_ID)
        financialDataQueryService.getSuppliesForProject(PROJECT_WITH_SUPPLY_ID) >> ([
                prepareSupplyDto(SUPPLY_ID)] as List)
    }

    def "createSupply should not create supply if project validation fails"() {
        given:
            SupplyDto supplyDto = this.prepareCreateSupplyDto(SUPPLIER_ID)
        when:
            this.supplyApplicationService.createSupply(NOT_EXISTING_PROJECT_ID, supplyDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createSupply should not create supply if createSupplyDto validation fails"() {
        given:
            mockValidatingSupplyJobDtoThrowsException()
            SupplyDto supplyDto = this.prepareCreateSupplyDto(SUPPLIER_ID)
        when:
            this.supplyApplicationService.createSupply(PROJECT_WITH_SUPPLY_ID, supplyDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createSupply should not create supply if supplier existence validation fails"() {
        given:
            SupplyDto supplyDto = this.prepareCreateSupplyDto(NOT_EXISTING_SUPPLIER_ID)
        when:
            this.supplyApplicationService.createSupply(PROJECT_WITH_SUPPLY_ID, supplyDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "createSupply should add supply to project financial data and save it via repository"() {
        given:
            SupplyDto supplyDto = this.prepareCreateSupplyDto(SUPPLIER_ID)
        when:
            this.supplyApplicationService.createSupply(PROJECT_WITHOUT_SUPPLY_ID, supplyDto)
        then:
            1 * this.projectFinancialDataRepository.save({
                ProjectFinancialData financialData ->
                    financialData.supplies.size() == 1
            })
    }

    def "createSupply should call onCreate on projectFinanceAwareObjectService"() {
        given:
            SupplyDto supplyDto = this.prepareCreateSupplyDto(SUPPLIER_ID)
        when:
            this.supplyApplicationService.createSupply(PROJECT_WITHOUT_SUPPLY_ID, supplyDto)
        then:
            1 * this.projectFinanceAwareObjectService.onCreate(PROJECT_WITHOUT_SUPPLY_ID)
    }

    def "updateSupply should not update supply if project validation fails"() {
        given:
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
        when:
            this.supplyApplicationService.updateSupply(PROJECT_WITHOUT_SUPPLY_ID, SUPPLY_ID, supplyDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateSupply should not update supply if supply on project existence fails"() {
        given:
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
        when:
            this.supplyApplicationService.updateSupply(PROJECT_WITHOUT_SUPPLY_ID, SUPPLY_ID, supplyDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateSupply should not update supply if update supply dto fails"() {
        given:
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
            mockValidatingUpdateSupplyDtoThrowsException();
        when:
            this.supplyApplicationService.updateSupply(PROJECT_WITH_SUPPLY_ID, SUPPLY_ID, supplyDto)
        then:
            thrown(IllegalArgumentException)
            0 * projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "updateSupply should update data on supply"() {
        given:
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
        when:
            SupplyDto updatedSupply =
                    this.supplyApplicationService.updateSupply(PROJECT_WITH_SUPPLY_ID, SUPPLY_ID, supplyDto)
        then:
            updatedSupply.note == NEW_NOTE
            updatedSupply.name == NEW_NAME
            updatedSupply.value == NEW_VALUE
            updatedSupply.hasInvoice == NEW_HAS_INVOICE
            updatedSupply.payable == NEW_PAYABLE
    }

    def "updateSupply should call onUpdate on projectFinanceAwareObjectService"() {
        given:
            SupplyDto supplyDto = this.prepareUpdateSupplyDto()
        when:
            this.supplyApplicationService.updateSupply(PROJECT_WITH_SUPPLY_ID, SUPPLY_ID, supplyDto)
        then:
            1 * this.projectFinanceAwareObjectService.onUpdate(PROJECT_WITH_SUPPLY_ID)
    }

    def "getSupply should not return supply if project existence fails"() {
        given:
        when:
            def supplyDto = this.supplyApplicationService.getSupply(NOT_EXISTING_PROJECT_ID, SUPPLY_ID)
        then:
            thrown(IllegalArgumentException)
            null == supplyDto
    }

    def "getSupply should validate supply on project existence"() {
        given:
        when:
            def supplyDto = this.supplyApplicationService.getSupply(PROJECT_WITHOUT_SUPPLY_ID, SUPPLY_ID)
        then:
            thrown(IllegalArgumentException)
            null == supplyDto
    }

    def "getSupply should return correct supply"() {
        given:
        when:
            SupplyDto supply = this.supplyApplicationService.getSupply(PROJECT_WITH_SUPPLY_ID, SUPPLY_ID)
        then:
            null != supply
            supply.id == SUPPLY_ID
    }

    def "deleteSupply should validate project existence"() {
        given:
        when:
            this.supplyApplicationService.deleteSupply(NOT_EXISTING_PROJECT_ID, SUPPLY_ID)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "deleteSupply should validate supply on project existence"() {
        given:
        when:
            this.supplyApplicationService.deleteSupply(PROJECT_WITHOUT_SUPPLY_ID, SUPPLY_ID)
        then:
            thrown(IllegalArgumentException)
            0 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData)
    }

    def "deleteSupply should remove supply from project"() {
        given:
        when:
            this.supplyApplicationService.deleteSupply(PROJECT_WITH_SUPPLY_ID, SUPPLY_ID)
        then:
            1 * this.projectFinancialDataRepository.save({ ProjectFinancialData financialData ->
                financialData.supplies.size() == 0
            })
    }

    def "deleteSupply should call onRemove on projectFinanceAwareObjectService"() {
        given:
        when:
            this.supplyApplicationService.deleteSupply(PROJECT_WITH_SUPPLY_ID, SUPPLY_ID)
        then:
            1 * this.projectFinanceAwareObjectService.onRemove(PROJECT_WITH_SUPPLY_ID)
    }

    def "getSupplyForProject should not return supplies if project existence fails"() {
        given:
        when:
            def supplies = this.supplyApplicationService.getSuppliesForProject(NOT_EXISTING_PROJECT_ID)
        then:
            thrown(IllegalArgumentException)
            null == supplies
    }

    def "getSupplyForProject should return list of all supplies for project with given projectId"() {
        given:
        when:
            def supplies = this.supplyApplicationService.getSuppliesForProject(PROJECT_WITH_SUPPLY_ID)
        then:
            null != supplies
            1 == supplies.size()
    }

    private SupplyDto prepareCreateSupplyDto(Long supplierId) {
        SupplyDto supplyDto = new SupplyDto(hasInvoice: HAS_INVOICE, payable: PAYABLE, supplierId: supplierId,
                value: VALUE)
        return supplyDto
    }

    private SupplyDto prepareUpdateSupplyDto() {
        SupplyDto supplyDto = new SupplyDto(hasInvoice: NEW_HAS_INVOICE, payable: NEW_PAYABLE, supplierId: SUPPLIER_ID,
                value: NEW_VALUE, name: NEW_NAME, note: NEW_NOTE)
        return supplyDto
    }

    private Supply prepareSupply(long supplyId) {
        Supply supply = new Supply(NAME, SUPPLIER_ID, VALUE, true, true)
        TestUtils.setFieldForObject(supply, "id", supplyId)
        return supply
    }

    private SupplyDto prepareSupplyDto(long supplyId) {
        SupplyDto supplyDto = new SupplyDto()
        supplyDto.id = supplyId
        return supplyDto
    }

    private void mockValidatingSupplyJobDtoThrowsException() {
        this.supplyValidator.validateCreateSupplyDto(_ as SupplyDto)
                >> { throw new IllegalArgumentException() }
    }

    private ProjectFinancialData prepareProjectFinancialDataWithoutSupply() {
        def projectFinancialData = new ProjectFinancialData()
        return projectFinancialData
    }

    private ProjectFinancialData prepareProjectFinancialDataWithSupply() {
        def projectFinancialData = new ProjectFinancialData()
        def supply = new Supply(NAME, SUPPLIER_ID, VALUE, HAS_INVOICE, PAYABLE)
        TestUtils.setFieldForObject(supply, "id", SUPPLY_ID)
        projectFinancialData.addSupply(supply)
        return projectFinancialData
    }

    private void mockValidatingUpdateSupplyDtoThrowsException() {
        this.supplyValidator.validateUpdateSupplyDto(_ as SupplyDto) >> {
            throw new IllegalArgumentException()
        }
    }
}
