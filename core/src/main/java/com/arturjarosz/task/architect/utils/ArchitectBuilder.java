package com.arturjarosz.task.architect.utils;

import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.BuilderUtils;

public class ArchitectBuilder extends AbstractBuilder<Architect, ArchitectBuilder> {

    private static final String ID = "id";

    public ArchitectBuilder() {
        super(Architect.class);
    }

    public ArchitectBuilder withId(Long id) {
        BuilderUtils.setFieldForObject(this.object, ID, id);
        return this;
    }
}
