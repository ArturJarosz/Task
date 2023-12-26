package com.arturjarosz.task.utils


import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.sharedkernel.testhelpers.AbstractBuilder

import static com.arturjarosz.task.sharedkernel.testhelpers.TestUtils.setFieldForObject

class StageBuilder extends AbstractBuilder<Stage, StageBuilder> {

    private static final String DEADLINE = "deadline"
    private static final String END_DATE = "endDate"
    private static final String ID = "id"
    private static final String INSTALLMENT = "installment"
    private static final String NAME = "name"
    private static final String NOTE = "note"
    private static final String START_DATE = "startDate"
    private static final String STAGE_TYPE = "stageType"
    private static final String STATUS = "status"
    private static final String TASKS = "tasks"

    StageBuilder() {
        super(Stage)
    }

    StageBuilder setStatusField(String fieldName, Object fieldValue) {
        setFieldForObject(this.object, fieldName, fieldValue)
        return this
    }

    StageBuilder withId(Long id) {
        setFieldForObject(this.object, ID, id)
        return this
    }

    StageBuilder withName(String name) {
        setFieldForObject(this.object, NAME, name)
        return this
    }

    StageBuilder withTask(Task task) {
        List<Task> tasks = new ArrayList<>()
        tasks.add(task)
        setFieldForObject(this.object, TASKS, tasks)
        return this
    }

    StageBuilder withTasks(List<Task> tasks) {
        setFieldForObject(this.object, TASKS, new ArrayList<>(tasks))
        return this
    }

    StageBuilder withStatus(StageStatus status) {
        this.setStatusField(STATUS, status)
        return this
    }
}
