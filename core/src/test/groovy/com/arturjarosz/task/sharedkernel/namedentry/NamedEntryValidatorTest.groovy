package com.arturjarosz.task.sharedkernel.namedentry

import com.arturjarosz.task.contractor.application.ContractorCategoryValidator
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository
import com.arturjarosz.task.contractor.query.ContractorQueryService
import com.arturjarosz.task.dto.StageTypeDto
import com.arturjarosz.task.finance.application.validator.CostCategoryValidator
import com.arturjarosz.task.finance.infrastructure.CostCategoryRepository
import com.arturjarosz.task.finance.query.FinancialDataQueryService
import com.arturjarosz.task.project.application.ProjectTypeValidator
import com.arturjarosz.task.project.application.StageTypeValidator
import com.arturjarosz.task.project.application.TaskTypeValidator
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository
import com.arturjarosz.task.project.infrastructure.repositor.StageTypeRepository
import com.arturjarosz.task.project.infrastructure.repositor.TaskTypeRepository
import com.arturjarosz.task.project.model.StageType
import com.arturjarosz.task.project.query.ProjectQueryService
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import com.arturjarosz.task.supplier.application.SupplierCategoryValidator
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository
import com.arturjarosz.task.supplier.query.SupplierQueryService
import spock.lang.Specification

class NamedEntryValidatorTest extends Specification {
    static final Long ENTRY_ID = 100L

    def stageTypeRepository = Mock(StageTypeRepository)
    def projectQueryService = Mock(ProjectQueryService)

    def validator = new StageTypeValidator(stageTypeRepository, projectQueryService)

    def "validateCreateDto should throw an exception for missing or blank name"() {
        when:
            this.validator.validateCreateDto(dto, StageTypeDto::getName)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == message
        where:
            dto                          | message
            null                         | "isNull.stageType"
            new StageTypeDto()           | "isNull.stageType.name"
            new StageTypeDto(name: "  ") | "isEmpty.stageType.name"
    }

    def "validateCreateDto should throw an exception for too long name"() {
        when:
            this.validator.validateCreateDto(new StageTypeDto(name: "a" * (AbstractNamedEntry.NAME_MAX_LENGTH + 1)),
                    StageTypeDto::getName)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.stageType.name.tooLong"
    }

    def "validateCreateDto should throw an exception when stripped name is already used"() {
        given:
            this.stageTypeRepository.existsByNameIgnoreCase("Concept") >> true
        when:
            this.validator.validateCreateDto(new StageTypeDto(name: " Concept "), StageTypeDto::getName)
        then:
            def exception = thrown(IllegalArgumentException)
            exception.message == "notValid.stageType.name.unique"
    }

    def "validateUpdateDto should check name uniqueness against other entries only"() {
        when:
            this.validator.validateUpdateDto(new StageTypeDto(name: "Concept"), StageTypeDto::getName, ENTRY_ID)
        then:
            1 * this.stageTypeRepository.existsByNameIgnoreCaseAndIdNot("Concept", ENTRY_ID) >> false
            noExceptionThrown()
    }

    def "validateExistence should look the entry up with a lock and throw an exception when it does not exist"() {
        when:
            this.validator.validateExistence(ENTRY_ID)
        then:
            1 * this.stageTypeRepository.findLockedById(ENTRY_ID) >> Optional.empty()
            def exception = thrown(ResourceNotFoundException)
            exception.message == "notExist.stageType"
    }

    def "validateExistence should not throw an exception when the entry exists"() {
        given:
            this.stageTypeRepository.findLockedById(ENTRY_ID) >> Optional.of(new StageType("Concept"))
        when:
            this.validator.validateExistence(ENTRY_ID)
        then:
            noExceptionThrown()
    }

    def "validateHasNoUsages should throw an exception only when an item uses the entry"() {
        given:
            this.projectQueryService.getNumberOfStagesByStageTypeIds([ENTRY_ID]) >> [(ENTRY_ID): usages]
        when:
            def exception = null
            try {
                this.validator.validateHasNoUsages(ENTRY_ID)
            } catch (IllegalArgumentException thrownException) {
                exception = thrownException
            }
        then:
            exception?.message == message
        where:
            usages || message
            2L     || "notValid.stageType.stages"
            0L     || null
    }

    def "each list should use its own message codes"() {
        expect:
            listValidator.usedEntryMessageCode == usedCode
            listValidator.nameUniqueMessageCode == uniqueCode
        where:
            listValidator                                                                                       || usedCode                                    | uniqueCode
            new ProjectTypeValidator(Mock(ProjectTypeRepository), Mock(ProjectQueryService))                     || "notValid.projectType.projects"             | "notValid.projectType.name.unique"
            new StageTypeValidator(Mock(StageTypeRepository), Mock(ProjectQueryService))                         || "notValid.stageType.stages"                 | "notValid.stageType.name.unique"
            new TaskTypeValidator(Mock(TaskTypeRepository), Mock(ProjectQueryService))                           || "notValid.taskType.tasks"                   | "notValid.taskType.name.unique"
            new CostCategoryValidator(Mock(CostCategoryRepository), Mock(FinancialDataQueryService))             || "notValid.costCategory.costs"               | "notValid.costCategory.name.unique"
            new ContractorCategoryValidator(Mock(ContractorCategoryRepository), Mock(ContractorQueryService))    || "notValid.contractorCategory.contractors"   | "notValid.contractorCategory.name.unique"
            new SupplierCategoryValidator(Mock(SupplierCategoryRepository), Mock(SupplierQueryService))          || "notValid.supplierCategory.suppliers"       | "notValid.supplierCategory.name.unique"
    }

    def "each list should count its usages through its own query"() {
        given:
            def contractorQueryService = Mock(ContractorQueryService)
            def listValidator = new ContractorCategoryValidator(Mock(ContractorCategoryRepository), contractorQueryService)
        when:
            def counts = listValidator.countUsages([ENTRY_ID])
        then:
            1 * contractorQueryService.getNumberOfContractorsByCategoryIds([ENTRY_ID]) >> [(ENTRY_ID): 3L]
            counts == [(ENTRY_ID): 3L]
    }
}
