package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.WorkTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;

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

    protected Task() {
        //needed by Hibernate
    }

    public Task(String name) {
        this.name = name;
    }
}
