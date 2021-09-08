package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.project.model.Contract;
import com.arturjarosz.task.project.model.Offer;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.stage.model.Stage;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.TestUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class ProjectBuilder extends AbstractBuilder<Project, ProjectBuilder> {

    private static final String ARCHITECT_ID = "architectId";
    private static final String ARRANGEMENT = "arrangement";
    private static final String CLIENT_ID = "clientId";
    private static final String END_DATE = "endDate";
    private static final String NAME = "name";
    private static final String NOTE = "note";
    private static final String ID = "id";
    private static final String STAGES = "stages";
    private static final String START_DATE = "startDate";
    private static final String STATUS = "status";

    public ProjectBuilder() {
        super(Project.class);
    }

    public ProjectBuilder withEndDate(LocalDate endDate) {
        TestUtils.setFieldForObject(this.object, END_DATE, endDate);
        return this;
    }

    public ProjectBuilder withId(Long id) {
        TestUtils.setFieldForObject(this.object, ID, id);
        return this;
    }

    public ProjectBuilder withName(String name) {
        TestUtils.setFieldForObject(this.object, NAME, name);
        return this;
    }

    public ProjectBuilder withNote(String note) {
        TestUtils.setFieldForObject(this.object, NOTE, note);
        return this;
    }

    public ProjectBuilder withStatus(ProjectStatus status) {
        TestUtils.setFieldForObject(this.object, STATUS, status);
        return this;
    }

    public ProjectBuilder withStartDate(LocalDate startDate) {
        TestUtils.setFieldForObject(this.object, START_DATE, startDate);
        return this;
    }

    public ProjectBuilder withStage(Stage stage) {
        Set<Stage> stages = new HashSet<>();
        stages.add(stage);
        TestUtils.setFieldForObject(this.object, STAGES, stages);
        return this;
    }

    public ProjectBuilder withStages(Set<Stage> stages) {
        TestUtils.setFieldForObject(this.object, STAGES, new HashSet<>(stages));
        return this;
    }

    public ProjectBuilder withClientId(Long clientId) {
        TestUtils.setFieldForObject(this.object, CLIENT_ID, clientId);
        return this;
    }

    public ProjectBuilder withArchitectId(Long architectId) {
        TestUtils.setFieldForObject(this.object, ARCHITECT_ID, architectId);
        return this;
    }

    public ProjectBuilder withOffer(Offer offer) {
        TestUtils.setFieldForObject(this.object, ARRANGEMENT, offer);
        return this;
    }

    public ProjectBuilder withContract(Contract contract) {
        TestUtils.setFieldForObject(this.object, ARRANGEMENT, contract);
        return this;
    }
}
