package com.arturjarosz.task.project.application

import com.arturjarosz.task.dto.StageDto
import com.arturjarosz.task.dto.StageTypeDto
import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository
import com.arturjarosz.task.finance.model.Installment
import com.arturjarosz.task.finance.model.ProjectFinancialData
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import com.arturjarosz.task.utils.ProjectBuilder
import com.arturjarosz.task.utils.StageBuilder
import spock.lang.Specification

class StageValidatorTest extends Specification {
    private static final Long PROJECT_ID = 1L
    private static final Long PROJECT_STAGE_WITHOUT_INSTALLMENT_ID = 2L
    private static final Long PROJECT_STAGE_WITH_INSTALLMENT_ID = 3L
    private static final Long EXISTING_STAGE_ID = 10L
    private static final Long NOT_EXISTING_STAGE_ID = 11l
    private static final Long STAGE_WITH_INSTALLMENT_ID = 20L
    private static final Long STAGE_WITHOUT_INSTALLMENT_ID = 21L

    private static final String STAGE_NAME = "stageName"

    def projectRepository = Mock(ProjectRepository)
    def projectFinancialDataRepository = Mock(ProjectFinancialDataRepository)

    def stageValidator = new StageValidator(projectRepository, projectFinancialDataRepository)

    def setup() {
        this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_STAGE_WITHOUT_INSTALLMENT_ID) >>
                prepareProjectFinancialData(false)
        this.projectFinancialDataRepository.getProjectFinancialDataByProjectId(PROJECT_STAGE_WITH_INSTALLMENT_ID) >>
                prepareProjectFinancialData(true)
    }

    def "validateCreateStageDto should throw an exception, when passed stageDto is null"() {
        given:
            def stageDto = null
        when:
            this.stageValidator.validateCreateStageDto(stageDto)
        then:
            IllegalArgumentException exception = thrown()
            exception.message == "isNull.stage"
    }

    def "validateCreateStageDto should throw an exception, on null name in passed stageDto"() {
        given:
            def stageDto = new StageDto(type: StageTypeDto.VISUALISATIONS)
        when:
            this.stageValidator.validateCreateStageDto(stageDto)
        then:
            IllegalArgumentException exception = thrown()
            exception.message == "isNull.stage.name"
    }

    def "validateCreateStageDto should throw an exception, on empty name in passed stageDto"() {
        given:
            def stageDto = new StageDto(name: "", type: StageTypeDto.VISUALISATIONS)
        when:
            this.stageValidator.validateCreateStageDto(stageDto)
        then:
            IllegalArgumentException exception = thrown()
            exception.message == "isEmpty.stage.name"
    }

    def "validateCreateStageDto should throw an exception, on null stage type in passed stageDto"() {
        given:
            def stageDto = new StageDto(name: STAGE_NAME)
        when:
            this.stageValidator.validateCreateStageDto(stageDto)
        then:
            IllegalArgumentException exception = thrown()
            exception.message == "isNull.stage.type"
    }

    def "validateCreateStageDto should not throw any exception on proper stageDto"() {
        given:
            def stageDto = new StageDto(name: STAGE_NAME, type: StageTypeDto.VISUALISATIONS)
        when:
            this.stageValidator.validateCreateStageDto(stageDto)
        then:
            noExceptionThrown()
    }

    def "validateUpdateStageDto should throw an exception, when passed stageDto is null"() {
        given:
            def stageDto = null
        when:
            this.stageValidator.validateUpdateStageDto(stageDto)
        then:
            IllegalArgumentException exception = thrown()
            exception.message == "isNull.stage"
    }

    def "validateUpdateStageDto should throw an exception, on null name in passed stageDto"() {
        given:
            def stageDto = new StageDto(type: StageTypeDto.VISUALISATIONS)
        when:
            this.stageValidator.validateUpdateStageDto(stageDto)
        then:
            IllegalArgumentException exception = thrown()
            exception.message == "isNull.stage.name"
    }

    def "validateUpdateStageDto should throw an exception, on empty name in passed stageDto"() {
        given:
            def stageDto = new StageDto(name: "", type: StageTypeDto.VISUALISATIONS)
        when:
            this.stageValidator.validateUpdateStageDto(stageDto)
        then:
            IllegalArgumentException exception = thrown()
            exception.message == "isEmpty.stage.name"
    }

    def "validateUpdateStageDto should throw an exception, on null stage type in passed stageDto"() {
        given:
            def stageDto = new StageDto(name: STAGE_NAME)
        when:
            this.stageValidator.validateUpdateStageDto(stageDto)
        then:
            IllegalArgumentException exception = thrown()
            exception.message == "isNull.stage.type"
    }

    def "validateUpdateStageDto should not throw any exception on proper stageDto"() {
        given:
            def stageDto = new StageDto(name: STAGE_NAME, type: StageTypeDto.VISUALISATIONS)
        when:
            this.stageValidator.validateUpdateStageDto(stageDto)
        then:
            noExceptionThrown()
    }

    def "validateExistenceStageInProject should throw an exception when stage not present on project"() {
        given:
            this.mockProjectRepositoryLoadProject()
            this.mockProjectRepositoryGetProject()
        when:
            this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, NOT_EXISTING_STAGE_ID)
        then:
            ResourceNotFoundException exception = thrown()
            exception.message == "notExist.project.stage"
    }

    def "validateExistenceStageInProject should not throw any exception when stage present on project"() {
        given:
            this.mockProjectRepositoryLoadProject()
            this.mockProjectRepositoryGetProject()
        when:
            this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, EXISTING_STAGE_ID)
        then:
            noExceptionThrown()
    }

    def "validateStageNotHavingInstallment should throw an exception, when stage has installment"() {
        given:
        when:
            this.stageValidator.validateStageNotHavingInstallment(PROJECT_STAGE_WITH_INSTALLMENT_ID,
                    STAGE_WITH_INSTALLMENT_ID)
        then:
            IllegalArgumentException exception = thrown()
            exception.message == "alreadySet.stage.installment"
    }

    def "validateStageNotHavingInstallment should not throw any exception, when stage has not installment"() {
        given:
        when:
            this.stageValidator.validateStageNotHavingInstallment(PROJECT_STAGE_WITHOUT_INSTALLMENT_ID,
                    STAGE_WITHOUT_INSTALLMENT_ID)
        then:
            noExceptionThrown()
    }

    private void mockProjectRepositoryLoadProject() {
        this.projectRepository.findById(PROJECT_ID) >> Optional.of(this.prepareProjectWithStage())
    }

    private void mockProjectRepositoryGetProject() {
        this.projectRepository.getReferenceById(PROJECT_ID) >> this.prepareProjectWithStage()
    }

    private Project prepareProjectWithStage() {
        return new ProjectBuilder()
                .withId(PROJECT_ID)
                .withStage(this.prepareStageWithId(EXISTING_STAGE_ID))
                .build()
    }

    private Stage prepareStageWithId(Long id) {
        return new StageBuilder().withId(id).build()
    }

    private ProjectFinancialData prepareProjectFinancialData(boolean stageHasInstallment) {
        def financialData = new ProjectFinancialData()
        if (stageHasInstallment) {
            def installment = new Installment()
            installment.setStageId(STAGE_WITH_INSTALLMENT_ID)
            financialData.addInstallment(installment)
        } else {
            TestUtils.setFieldForObject(financialData, "installments", new HashSet<>())
        }
        return financialData
    }

}
