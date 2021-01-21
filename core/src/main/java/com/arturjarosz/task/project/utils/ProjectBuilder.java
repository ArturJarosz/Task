package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.BuilderUtils;

public class ProjectBuilder extends AbstractBuilder<Project, ProjectBuilder> {

    public ProjectBuilder(Project project) {
        super(Project.class);
    }

    public ProjectBuilder withName(String name) {
        BuilderUtils.setFieldForObject(this.object, "name", name);
        return this;
    }

}
