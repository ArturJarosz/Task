package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "COST")
public class Cost extends AbstractEntity {

    private static final long serialVersionUID = 4833869293487851155L;
    @Column(name = "name", nullable = false)
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COST_VALUE"))
    private Money value;

    @Column(name = "CATEGORY", nullable = false)
    @Enumerated(EnumType.STRING)
    private CostCategory category;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PROJECT_ID")
    private Long projectId;

    protected Cost() {
        //needed by Hibernate
    }

    public Cost(String name, Money value, CostCategory category, LocalDate date, String description) {
        this.name = name;
        this.value = value;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    public void setValue(Money value) {
        this.value = value;
    }

    protected String getName() {
        return this.name;
    }

    protected Money getValue() {
        return this.value;
    }

    protected CostCategory getCategory() {
        return this.category;
    }

    protected LocalDate getDate() {
        return this.date;
    }

    protected String getDescription() {
        return this.description;
    }
}
