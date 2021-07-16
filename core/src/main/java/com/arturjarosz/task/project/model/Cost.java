package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@SequenceGenerator(name = "cost_sequence_generator", sequenceName = "cost_sequence", allocationSize = 1)
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

    @Column(name = "NOTE")
    private String note;

    @Column(name = "PROJECT_ID")
    private Long projectId;

    protected Cost() {
        //needed by Hibernate
    }

    public Cost(String name, Money value, CostCategory category, LocalDate date, String note) {
        this.name = name;
        this.value = value;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public void setValue(Money value) {
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public Money getValue() {
        return this.value;
    }

    public CostCategory getCategory() {
        return this.category;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getNote() {
        return this.note;
    }

    public void updateCost(String name, Double value, LocalDate date, String note, CostCategory category) {
        this.name = name;
        this.value = new Money(value);
        this.date = date;
        this.note = note;
        this.category = category;
    }
}
