package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.model.TaskType;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.sharedkernel.testhelpers.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils;

public class TaskBuilder extends AbstractBuilder<Task, TaskBuilder> {
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String STATUS = "status";
    private static final String TYPE = "type";

    public TaskBuilder() {
        super(Task.class);
    }

    public TaskBuilder withId(Long id) {
        TestUtils.setFieldForObject(this.object, ID, id);
        return this;
    }

    public TaskBuilder withName(String name) {
        TestUtils.setFieldForObject(this.object, NAME, name);
        return this;
    }

    public TaskBuilder withType(TaskType type) {
        TestUtils.setFieldForObject(this.object, TYPE, type);
        return this;
    }

    public TaskBuilder withStatus(TaskStatus taskStatus) {
        TestUtils.setFieldForObject(this.object, STATUS, taskStatus);
        return this;
    }
}
