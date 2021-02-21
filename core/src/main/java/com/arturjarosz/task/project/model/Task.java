package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "task_sequence", allocationSize = 1)
@Table(name = "TASK")
public class Task extends AbstractEntity {
    private static final long serialVersionUID = 9208147376126632528L;

    @Column(name = "NAME", nullable = false)
    private String name;

    /*    //TODO: implement with TA-62
    @Column(name = "STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private WorkStatus status;

    //TODO: TA-95
    @Embedded
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

    public Task() {
        //needed by Hibernate
    }

    public Task(String name, TaskType taskType) {
        this.name = name;
        this.type = taskType;
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
}
