package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.task.TaskStatus;
import com.arturjarosz.task.project.status.task.TaskWorkflow;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.status.WorkflowAware;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@SequenceGenerator(name = "task_sequence_generator", sequenceName = "task_sequence", allocationSize = 1)
@Table(name = "TASK")
public class Task extends AbstractEntity implements WorkflowAware<TaskStatus> {
    private static final long serialVersionUID = 9208147376126632528L;

    @Column(name = "NAME", nullable = false)
    private String name;

    //TODO: TA-95
    /* @Embedded
    private WorkTime workTime;*/

    @Enumerated(EnumType.STRING)
    @Column(name = "TASK_TYPE")
    private TaskType type;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TaskStatus status;

    @Column(name = "WORKFLOW_NAME", nullable = false)
    private String workflowName;

    public Task() {
        //needed by Hibernate
    }

    public Task(String name, TaskType taskType, TaskWorkflow taskWorkflow) {
        this.name = name;
        this.type = taskType;
        this.workflowName = taskWorkflow.getName();
    }

    public String getName() {
        return this.name;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void update(TaskInnerDto taskInnerDto) {
        this.name = taskInnerDto.getName();
        this.type = taskInnerDto.getTaskType();
        this.startDate = taskInnerDto.getStartDate();
        this.endDate = taskInnerDto.getEndDate();
        this.note = taskInnerDto.getNote();
    }

    @Override
    public TaskStatus getStatus() {
        return this.status;
    }

    @Override
    public String getWorkflowName() {
        return this.workflowName;
    }

    @Override
    public void changeStatus(TaskStatus newStatus) {
        this.status = newStatus;
    }

    public TaskType getType() {
        return this.type;
    }

    public String getNote() {
        return this.note;
    }
}
