package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.model.StageType;
import com.arturjarosz.task.project.model.Task;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.arturjarosz.task.sharedkernel.utils.TestUtils.setFieldForObject;

public class StageBuilder extends AbstractBuilder<Stage, StageBuilder> {

    private static final String DEADLINE = "deadline";
    private static final String END_DATE = "endDate";
    private static final String ID = "id";
    private static final String INSTALLMENT = "installment";
    private static final String NAME = "name";
    private static final String NOTE = "note";
    private static final String START_DATE = "startDate";
    private static final String STAGE_TYPE = "stageType";
    private static final String STATUS = "status";
    private static final String TASKS = "tasks";

    public StageBuilder() {
        super(Stage.class);
    }

    public StageBuilder setStatusField(String fieldName, Object fieldValue) {
        setFieldForObject(this.object, fieldName, fieldValue);
        return this;
    }

    public StageBuilder withId(Long id) {
        setFieldForObject(this.object, ID, id);
        return this;
    }

    public StageBuilder withName(String name) {
        setFieldForObject(this.object, NAME, name);
        return this;
    }

    public StageBuilder withStartDate(LocalDate startDate) {
        setFieldForObject(this.object, START_DATE, startDate);
        return this;
    }

    public StageBuilder withEndDate(LocalDate endDate) {
        setFieldForObject(this.object, END_DATE, endDate);
        return this;
    }

    public StageBuilder withDeadline(LocalDate deadline) {
        setFieldForObject(this.object, DEADLINE, deadline);
        return this;
    }

    public StageBuilder withNote(String note) {
        setFieldForObject(this.object, NOTE, note);
        return this;
    }

    public StageBuilder withStageType(StageType stageType) {
        setFieldForObject(this.object, STAGE_TYPE, stageType);
        return this;
    }

    public StageBuilder withInstallment(Installment installment) {
        setFieldForObject(this.object, INSTALLMENT, installment);
        return this;
    }

    public StageBuilder withTask(Task task) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);
        setFieldForObject(this.object, TASKS, tasks);
        return this;
    }

    public StageBuilder withStatus(StageStatus status) {
        return this.setStatusField(STATUS, status);
    }
}
