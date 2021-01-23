package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.BuilderUtils;

public class ProjectBuilder extends AbstractBuilder<Project, ProjectBuilder> {

    private static final String NAME = "name";
    private static final String ID = "id";

    public ProjectBuilder() {
        super(Project.class);
    }

    public ProjectBuilder withId(Long id) {
        BuilderUtils.setFieldForObject(this.object, ID, id);
        return this;
    }

    public ProjectBuilder withName(String name) {
        BuilderUtils.setFieldForObject(this.object, NAME, name);
        return this;
    }

}
