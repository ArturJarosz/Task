package com.arturjarosz.task.project.application

import com.arturjarosz.task.project.application.dto.StageDto
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Installment
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.StageType
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.project.utils.StageBuilder
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException
import spock.lang.Specification

class StageValidatorTest extends Specification {
    private static final Long PROJECT_ID = 2L;
    private static final Long EXISTING_STAGE_ID = 3L;
    private static final Long NOT_EXISTING_STAGE_ID = 4l;
    private static final Long STAGE_WITH_INSTALLMENT_ID = 5L;
    private static final Long STAGE_WITHOUT_INSTALLMENT_ID = 6L;

    private static final String STAGE_NAME = "stageName";

    def projectRepository = Mock(ProjectRepositoryImpl);
    def projectQueryService = Mock(ProjectQueryServiceImpl);

    def stageValidator = new StageValidator(projectRepository, projectQueryService);

    def "validateCreateStageDto should throw an exception, when passed stageDto is null"() {
        given:
            StageDto stageDto = null;
        when:
            this.stageValidator.validateCreateStageDto(stageDto);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "isNull.stage";
    }

    def "validateCreateStageDto should throw an exception, on null name in passed stageDto"() {
        given:
            StageDto stageDto = new StageDto();
            stageDto.setStageType(StageType.VISUALISATIONS);
        when:
            this.stageValidator.validateCreateStageDto(stageDto);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "isNull.stage.name";
    }

    def "validateCreateStageDto should throw an exception, on empty name in passed stageDto"() {
        given:
            StageDto stageDto = new StageDto();
            stageDto.setName("");
            stageDto.setStageType(StageType.VISUALISATIONS);
        when:
            this.stageValidator.validateCreateStageDto(stageDto);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "isEmpty.stage.name";
    }

    def "validateCreateStageDto should throw an exception, on null stage type in passed stageDto"() {
        given:
            StageDto stageDto = new StageDto();
            stageDto.setName(STAGE_NAME);
        when:
            this.stageValidator.validateCreateStageDto(stageDto);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "isNull.stage.type";
    }

    def "validateCreateStageDto should not throw any exception on proper stageDto"() {
        given:
            StageDto stageDto = new StageDto();
            stageDto.setName(STAGE_NAME);
            stageDto.setStageType(StageType.VISUALISATIONS);
        when:
            this.stageValidator.validateCreateStageDto(stageDto);
        then:
            noExceptionThrown();
    }

    def "validateUpdateStageDto should throw an exception, when passed stageDto is null"() {
        given:
            StageDto stageDto = null;
        when:
            this.stageValidator.validateUpdateStageDto(stageDto);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "isNull.stage";
    }

    def "validateUpdateStageDto should throw an exception, on null name in passed stageDto"() {
        given:
            StageDto stageDto = new StageDto();
            stageDto.setStageType(StageType.VISUALISATIONS);
        when:
            this.stageValidator.validateUpdateStageDto(stageDto);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "isNull.stage.name";
    }

    def "validateUpdateStageDto should throw an exception, on empty name in passed stageDto"() {
        given:
            StageDto stageDto = new StageDto();
            stageDto.setName("");
            stageDto.setStageType(StageType.VISUALISATIONS);
        when:
            this.stageValidator.validateUpdateStageDto(stageDto);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "isEmpty.stage.name";
    }

    def "validateUpdateStageDto should throw an exception, on null stage type in passed stageDto"() {
        given:
            StageDto stageDto = new StageDto();
            stageDto.setName(STAGE_NAME);
        when:
            this.stageValidator.validateUpdateStageDto(stageDto);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "isNull.stage.type";
    }

    def "validateUpdateStageDto should not throw any exception on proper stageDto"() {
        given:
            StageDto stageDto = new StageDto();
            stageDto.setName(STAGE_NAME);
            stageDto.setStageType(StageType.VISUALISATIONS);
        when:
            this.stageValidator.validateUpdateStageDto(stageDto);
        then:
            noExceptionThrown();
    }

    def "validateExistenceStageInProject should throw an exception when stage not present on project"() {
        given:
            this.mockProjectRepositoryLoadProject();
        when:
            this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, NOT_EXISTING_STAGE_ID);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "notExists.project.stage";
    }

    def "validateExistenceStageInProject should not throw any exception when stage present on project"() {
        given:
            this.mockProjectRepositoryLoadProject();
        when:
            this.stageValidator.validateExistenceOfStageInProject(PROJECT_ID, EXISTING_STAGE_ID);
        then:
            noExceptionThrown();
    }

    def "validateStageNotHavingInstallment should throw an exception, when stage has installment"() {
        given:
            this.mockProjectQueryServiceStageWithInstallment()
        when:
            this.stageValidator.validateStageNotHavingInstallment(STAGE_WITH_INSTALLMENT_ID);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "alreadySet.stage.installment";
    }

    def "validateStageNotHavingInstallment should not throw any exception, when stage has not installment"() {
        given:
            this.mockProjectQueryServiceStageWithoutInstallment()
        when:
            this.stageValidator.validateStageNotHavingInstallment(STAGE_WITHOUT_INSTALLMENT_ID);
        then:
            noExceptionThrown();
    }

    def "validateStageHavingInstallment should throw an exception, when stage has not installment"() {
        given:
            this.mockProjectQueryServiceStageWithoutInstallment()
        when:
            this.stageValidator.validateStageHavingInstallment(STAGE_WITHOUT_INSTALLMENT_ID);
        then:
            IllegalArgumentException exception = thrown();
            exception.message == "notExists.stage.installment";
    }

    def "validateStageHavingInstallment should not throw any exception, when stage has installment"() {
        given:
            this.mockProjectQueryServiceStageWithInstallment()
        when:
            this.stageValidator.validateStageHavingInstallment(STAGE_WITH_INSTALLMENT_ID);
        then:
            noExceptionThrown();
    }

    private void mockProjectRepositoryLoadProject() {
        this.projectRepository.load(PROJECT_ID) >> this.prepareProjectWithStage();
    }

    private void mockProjectQueryServiceStageWithInstallment() {
        this.projectQueryService.getStageById(STAGE_WITH_INSTALLMENT_ID) >> this.prepareStageWithInstallment();
    }

    private void mockProjectQueryServiceStageWithoutInstallment() {
        this.projectQueryService.getStageById(STAGE_WITHOUT_INSTALLMENT_ID) >> this.prepareStageWithoutInstallment();
    }

    private Project prepareProjectWithStage() {
        return new ProjectBuilder()
                .withId(PROJECT_ID)
                .withStage(this.prepareStageWithId(EXISTING_STAGE_ID))
                .build();
    }

    private Stage prepareStageWithId(Long id) {
        return new StageBuilder().withId(id).build();
    }

    private Stage prepareStageWithoutInstallment() {
        return new StageBuilder()
                .withId(STAGE_WITHOUT_INSTALLMENT_ID)
                .build();
    }

    private Stage prepareStageWithInstallment() {
        return new StageBuilder()
                .withId(STAGE_WITH_INSTALLMENT_ID)
                .withInstallment(Mock(Installment))
                .build();
    }
}
