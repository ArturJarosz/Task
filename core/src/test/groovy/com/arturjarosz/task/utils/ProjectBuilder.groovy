package com.arturjarosz.task.utils

import com.arturjarosz.task.contract.model.Contract
import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.sharedkernel.testhelpers.AbstractBuilder
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils

import java.time.LocalDate

class ProjectBuilder extends AbstractBuilder<Project, ProjectBuilder> {

    private static final String ARCHITECT_ID = "architectId"
    private static final String ARRANGEMENT = "arrangement"
    private static final String CLIENT_ID = "clientId"
    private static final String END_DATE = "endDate"
    private static final String NAME = "name"
    private static final String NOTE = "note"
    private static final String ID = "id"
    private static final String STAGES = "stages"
    private static final String START_DATE = "startDate"
    private static final String STATUS = "status"
    private static final String PROJECT_TYPE = "projectType"

    ProjectBuilder() {
        super(Project)
    }

    ProjectBuilder withEndDate(LocalDate endDate) {
        TestUtils.setFieldForObject(this.object, END_DATE, endDate)
        return this
    }

    ProjectBuilder withId(Long id) {
        TestUtils.setFieldForObject(this.object, ID, id)
        return this
    }

    ProjectBuilder withName(String name) {
        TestUtils.setFieldForObject(this.object, NAME, name)
        return this
    }

    ProjectBuilder withNote(String note) {
        TestUtils.setFieldForObject(this.object, NOTE, note)
        return this
    }

    ProjectBuilder withStatus(ProjectStatus status) {
        TestUtils.setFieldForObject(this.object, STATUS, status)
        return this
    }

    ProjectBuilder withStartDate(LocalDate startDate) {
        TestUtils.setFieldForObject(this.object, START_DATE, startDate)
        return this
    }

    ProjectBuilder withStage(Stage stage) {
        Set<Stage> stages = new HashSet<>()
        stages.add(stage)
        TestUtils.setFieldForObject(this.object, STAGES, stages)
        return this
    }

    ProjectBuilder withStages(Set<Stage> stages) {
        TestUtils.setFieldForObject(this.object, STAGES, new HashSet<>(stages))
        return this
    }

    ProjectBuilder withClientId(Long clientId) {
        TestUtils.setFieldForObject(this.object, CLIENT_ID, clientId)
        return this
    }

    ProjectBuilder withArchitectId(Long architectId) {
        TestUtils.setFieldForObject(this.object, ARCHITECT_ID, architectId)
        return this
    }

    ProjectBuilder withContract(Contract contract) {
        TestUtils.setFieldForObject(this.object, ARRANGEMENT, contract)
        return this
    }

    ProjectBuilder withType(ProjectType projectType) {
        TestUtils.setFieldForObject(this.object, PROJECT_TYPE, projectType)
        return this
    }
}
