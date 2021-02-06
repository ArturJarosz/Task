package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.TestUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectBuilder extends AbstractBuilder<Project, ProjectBuilder> {

    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String STAGES = "stages";

    public ProjectBuilder() {
        super(Project.class);
    }

    public ProjectBuilder withId(Long id) {
        TestUtils.setFieldForObject(this.object, ID, id);
        return this;
    }

    public ProjectBuilder withName(String name) {
        TestUtils.setFieldForObject(this.object, NAME, name);
        return this;
    }

    public ProjectBuilder withStage(Stage stage) {
        List<Stage> stages = new ArrayList<>();
        stages.add(stage);
        TestUtils.setFieldForObject(this.object, STAGES, stages);
        return this;
    }

}
