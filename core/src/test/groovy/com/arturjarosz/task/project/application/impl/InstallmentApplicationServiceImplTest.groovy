package com.arturjarosz.task.project.application.impl

import com.arturjarosz.task.finance.model.FinancialData
import com.arturjarosz.task.project.application.ProjectValidator
import com.arturjarosz.task.project.application.StageValidator
import com.arturjarosz.task.project.application.dto.InstallmentDto
import com.arturjarosz.task.project.domain.impl.InstallmentDomainServiceImpl
import com.arturjarosz.task.project.infrastructure.repositor.impl.ProjectRepositoryImpl
import com.arturjarosz.task.project.model.Installment
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.query.impl.ProjectQueryServiceImpl
import com.arturjarosz.task.project.utils.InstallmentBuilder
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.project.utils.StageBuilder
import com.arturjarosz.task.sharedkernel.model.Money
import com.arturjarosz.task.sharedkernel.utils.TestUtils
import com.arturjarosz.task.supervision.utils.FinancialDataBuilder
import spock.lang.Specification

import java.time.LocalDate

class InstallmentApplicationServiceImplTest extends Specification {

    private static final Long INSTALLMENT_ID = 1L;
    private static final Long EXISTING_STAGE_ID = 10L;
    private static final Long STAGE_WITH_INSTALLMENT_ID = 20L;
    private static final Long STAGE_WITHOUT_INSTALLMENT_ID = 30L;
    private static final Long NOT_EXISTING_STAGE_ID = 99L;
    private static final Long EXISTING_PROJECT_ID = 100L;
    private static final Long EXISTING_PROJECT_WITH_INSTALLMENT_ID = 200L;
    private static final Long NOT_EXISTING_PROJECT_ID = 999L;
    private static final BigDecimal OLD_VALUE = new BigDecimal(100.00);
    private static final BigDecimal NEW_VALUE = new BigDecimal(200.00);
    private static final String PROJECT_NAME = "project name";

    private FinancialData financialData = new FinancialDataBuilder().withValue(new Money(OLD_VALUE)).withHasInvoice(
            true).build();
    private Installment installment = new InstallmentBuilder().withFinancialData(financialData)
            .build();
    private Stage stageWithoutInstallment = new StageBuilder().withId(STAGE_WITHOUT_INSTALLMENT_ID)
            .build();
    private Stage stageWithInstallment = new StageBuilder().withId(STAGE_WITH_INSTALLMENT_ID)
            .withInstallment(installment).build();
    private Project project = new ProjectBuilder().withName(PROJECT_NAME).withId(EXISTING_PROJECT_ID)
            .withStage(stageWithoutInstallment)
            .build();
    private Project projectWithInstallment = new ProjectBuilder().withName(PROJECT_NAME)
            .withId(EXISTING_PROJECT_WITH_INSTALLMENT_ID).withStage(stageWithInstallment).build();

    def installmentDomainService = new InstallmentDomainServiceImpl();


    def projectRepository = Mock(ProjectRepositoryImpl) {
        load(NOT_EXISTING_PROJECT_ID) >> { null };
        load(EXISTING_PROJECT_ID) >> { project };
        load(EXISTING_PROJECT_WITH_INSTALLMENT_ID) >> { projectWithInstallment };
        save(_ as Project) >> {
            Set<Stage> stages = this.project.getStages();
            Installment installmentFromStage = stages.iterator().next().getInstallment();
            if (installmentFromStage != null) {
                TestUtils.setFieldForObject(installmentFromStage, "id", INSTALLMENT_ID);
            }
            return this.project;
        }
    }

    def projectQueryService = Mock(ProjectQueryServiceImpl) {
        getStageById(STAGE_WITH_INSTALLMENT_ID) >> {
            return stageWithInstallment;
        }
        getStageById(STAGE_WITHOUT_INSTALLMENT_ID) >> { return stageWithoutInstallment };

    }

    def projectValidator = new ProjectValidator(projectRepository, projectQueryService);

    def stageValidator = new StageValidator(projectRepository, projectQueryService);

    def installmentApplicationService = new InstallmentApplicationServiceImpl(installmentDomainService,
            projectValidator, projectQueryService, projectRepository, stageValidator);

    def "createInstallment should throw an exception, when project with given id does not exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setValue(OLD_VALUE);
        when:
            this.installmentApplicationService.
                    createInstallment(NOT_EXISTING_PROJECT_ID, EXISTING_STAGE_ID, installmentDto);
        then:
            Exception exception = thrown();
            exception.message == "notExist.project"
    }

    def "createInstallment should throw an exception, when stage with given id does not exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setValue(OLD_VALUE);
        when:
            this.installmentApplicationService.
                    createInstallment(EXISTING_PROJECT_ID, NOT_EXISTING_STAGE_ID, installmentDto);
        then:
            Exception exception = thrown();
            exception.message == "notExist.project.stage"
    }

    def "createInstallment should throw an exception, when trying to add installment to stage that has installment"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setValue(OLD_VALUE);
        when:
            this.installmentApplicationService.
                    createInstallment(EXISTING_PROJECT_WITH_INSTALLMENT_ID, STAGE_WITH_INSTALLMENT_ID,
                            installmentDto);
        then:
            Exception exception = thrown();
            exception.message == "alreadySet.stage.installment";
    }

    def "createInstallment should throw an exception, when dto is not proper"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
        when:
            this.installmentApplicationService.createInstallment(EXISTING_PROJECT_ID, STAGE_WITHOUT_INSTALLMENT_ID,
                    installmentDto);
        then:
            Exception exception = thrown();
            exception.message == "isNull.installment.value";
    }

    def "createInstallment should create installment if dto is proper, and project and stage exist for given ids"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setValue(OLD_VALUE);
            installmentDto.setHasInvoice(true);
        when:
            InstallmentDto createdInstallmentDto = this.installmentApplicationService.
                    createInstallment(EXISTING_PROJECT_ID, STAGE_WITHOUT_INSTALLMENT_ID, installmentDto);
        then:
            noExceptionThrown();
            createdInstallmentDto.getId() == INSTALLMENT_ID;
    }

    def "updateInstallment should throw an error when project with given id does not exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setValue(OLD_VALUE);
        when:
            this.installmentApplicationService.
                    updateInstallment(NOT_EXISTING_PROJECT_ID, EXISTING_STAGE_ID, installmentDto)
        then:
            Exception exception = thrown();
            exception.message == "notExist.project"
    }

    def "updateInstallment should throw an error when stage with given id does not exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setValue(OLD_VALUE);
        when:
            this.installmentApplicationService.
                    updateInstallment(EXISTING_PROJECT_ID, NOT_EXISTING_STAGE_ID, installmentDto)
        then:
            Exception exception = thrown();
            exception.message == "notExist.project.stage"
    }

    def "updateInstallment should update installment if dto is correct and both project and stage exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setValue(NEW_VALUE);
            installmentDto.setHasInvoice(true);
        when:
            this.installmentApplicationService.
                    updateInstallment(EXISTING_PROJECT_WITH_INSTALLMENT_ID, STAGE_WITH_INSTALLMENT_ID, installmentDto)
        then:
            noExceptionThrown();
            1 * this.projectRepository.save({
                Project projectResult ->
                    Set<Stage> stagesFromProject = projectResult.getStages();
                    Installment installmentFromUpdate = stagesFromProject.iterator().next().getInstallment();
                    installmentFromUpdate.getAmount().getValue().doubleValue() == NEW_VALUE;
            });
    }

    def "removeInstallment should throw an exception when project with given id does not exist"() {
        given:
        when:
            this.installmentApplicationService.removeInstallment(NOT_EXISTING_PROJECT_ID, EXISTING_STAGE_ID);
        then:
            Exception exception = thrown();
            exception.message == "notExist.project";
    }

    def "removeInstallment should throw an exception when stage id with given id does not exist"() {
        given:
        when:
            this.installmentApplicationService.removeInstallment(EXISTING_PROJECT_ID, NOT_EXISTING_STAGE_ID);
        then:
            Exception exception = thrown();
            exception.message == "notExist.project.stage";
    }

    def "removeInstallment should remove installment from stage if both project and stage exist"() {
        given:
        when:
            this.installmentApplicationService.removeInstallment(EXISTING_PROJECT_WITH_INSTALLMENT_ID,
                    STAGE_WITH_INSTALLMENT_ID);
        then:
            1 * this.projectRepository.save({
                Project projectResult ->
                    Set<Stage> stagesFromProject = projectResult.getStages();
                    stagesFromProject.iterator().next().getInstallment() == null;
            })
    }

    def "payInstallment should throw an exception if project with given id does not exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setPaymentDate(LocalDate.now().minusDays(1));
        when:
            this.installmentApplicationService.payInstallment(NOT_EXISTING_PROJECT_ID,
                    STAGE_WITH_INSTALLMENT_ID, installmentDto);
        then:
            Exception exception = thrown();
            exception.message == "notExist.project";
    }

    def "payInstallment should throw an exception if stage with given id does not exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setPaymentDate(LocalDate.now().minusDays(1));
        when:
            this.installmentApplicationService.payInstallment(EXISTING_PROJECT_WITH_INSTALLMENT_ID,
                    NOT_EXISTING_STAGE_ID, installmentDto);
        then:
            Exception exception = thrown();
            exception.message == "notExist.project.stage";
    }

    def "payInstallment should throw an exception if dto is not correct"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setPaymentDate(LocalDate.now().plusDays(2));
        when:
            this.installmentApplicationService.payInstallment(EXISTING_PROJECT_WITH_INSTALLMENT_ID,
                    STAGE_WITH_INSTALLMENT_ID, installmentDto);
        then:
            Exception exception = thrown();
            exception.message == "notValid.installment.payDate";
    }

    def "payInstallment should change installment status to paid if dto is correct and both project and stage exist"() {
        given:
            InstallmentDto installmentDto = new InstallmentDto();
            installmentDto.setPaymentDate(LocalDate.now().minusDays(2));
        when:
            this.installmentApplicationService.payInstallment(EXISTING_PROJECT_WITH_INSTALLMENT_ID,
                    STAGE_WITH_INSTALLMENT_ID, installmentDto);
        then:
            noExceptionThrown();
            1 * this.projectRepository.save({
                Project projectResult ->
                    Set<Stage> stagesFromProject = projectResult.getStages();
                    stagesFromProject.iterator().next().getInstallment().isPaid();
            })
    }

    def "getInstallmentList should throw an exception if project with given id does not exist"() {
        given:
        when:
            List<Installment> installments =
                    this.installmentApplicationService.getInstallmentList(NOT_EXISTING_PROJECT_ID);
        then:
            Exception exception = thrown();
            exception.message == "notExist.project";
    }

    def "getInstallmentList should return list of all installments for given project"() {
        given:
        when:
            List<Installment> installments =
                    this.installmentApplicationService.getInstallmentList(EXISTING_PROJECT_WITH_INSTALLMENT_ID);
        then:
            noExceptionThrown();
            installments.size() == 1;
    }

    def "getInstallment should throw an exception if project with given id does not exist"() {
        given:
        when:
            InstallmentDto resultInstallment =
                    this.installmentApplicationService.
                            getInstallment(NOT_EXISTING_PROJECT_ID, STAGE_WITH_INSTALLMENT_ID);
        then:
            Exception exception = thrown();
            exception.message == "notExist.project";
    }

    def "getInstallment should throw an exception if stage with given id does not exist"() {
        given:
        when:
            InstallmentDto resultInstallment =
                    this.installmentApplicationService.
                            getInstallment(EXISTING_PROJECT_WITH_INSTALLMENT_ID, NOT_EXISTING_STAGE_ID);
        then:
            Exception exception = thrown();
            exception.message == "notExist.project.stage";
    }

    def "getInstallment should return installment when both project and stage exist"() {
        given:
        when:
            InstallmentDto resultInstallment =
                    this.installmentApplicationService.
                            getInstallment(EXISTING_PROJECT_WITH_INSTALLMENT_ID, STAGE_WITH_INSTALLMENT_ID);
        then:
            noExceptionThrown();
            resultInstallment.getValue() == OLD_VALUE;
    }
}
