package com.arturjarosz.task.finance.application.impl

import com.arturjarosz.task.finance.infrastructure.impl.ProjectFinancialDataRepositoryImpl
import com.arturjarosz.task.finance.model.ProjectFinancialData
import com.arturjarosz.task.project.application.ProjectValidator
import spock.lang.Specification

class ProjectFinancialDataApplicationServiceImplTest extends Specification {
    private static final Long PROJECT_ID = 1L;

    def projectFinancialDataRepository = Mock(ProjectFinancialDataRepositoryImpl);
    def projectValidator = Mock(ProjectValidator);

    def projectFinancialDataApplicationService = new ProjectFinancialDataApplicationServiceImpl
            (projectFinancialDataRepository, projectValidator);

    def "createProjectFinancialData should call validateProjectExistence on projectValidator"() {
        given:
        when:
            this.projectFinancialDataApplicationService.createProjectFinancialData(PROJECT_ID);
        then:
            1 * this.projectValidator.validateProjectExistence(PROJECT_ID);
    }

    def "createProjectFinancialData should save projectFinancialData with repository"() {
        given:
        when:
            this.projectFinancialDataApplicationService.createProjectFinancialData(PROJECT_ID);
        then:
            1 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData);
    }

    def "createProjectFinancialData should return projectFinancialData with correct projectId"() {
        given:
            mockProjectFinancialDataRepositorySave();
        when:
            ProjectFinancialData projectFinancialData = this.projectFinancialDataApplicationService
                    .createProjectFinancialData(PROJECT_ID);
        then:
            projectFinancialData.getProjectId() == PROJECT_ID;
    }

    private void mockProjectFinancialDataRepositorySave() {
        ProjectFinancialData projectFinancialData = new ProjectFinancialData(PROJECT_ID);
        1 * this.projectFinancialDataRepository.save(_ as ProjectFinancialData) >> projectFinancialData;
    }

}
