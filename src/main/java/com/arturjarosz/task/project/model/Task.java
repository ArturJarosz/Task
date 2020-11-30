package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.WorkTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "TASK")
public class Task extends AbstractEntity {
    private static final long serialVersionUID = 9208147376126632528L;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private WorkStatus status;

    @Embedded
    private WorkTime workTime;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    public Task() {
        //needed by Hibernate
    }

    public Task(String name) {
        this.name = name;
    }

    public Task(String name, WorkStatus status, WorkTime workTime) {
        this.name = name;
        this.status = status;
        this.workTime = workTime;
    }

    public String getName() {
        return this.name;
    }

    public WorkStatus getStatus() {
        return this.status;
    }

    public WorkTime getWorkTime() {
        return this.workTime;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

}
